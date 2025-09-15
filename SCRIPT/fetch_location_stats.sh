#!/bin/bash

# ========================
# Configuration
# ========================
BASE_URL="https://api.intra.42.fr/v2/users"
INPUT_FILE="campus65_users.json"
OUTPUT_FILE="campus65_location_stats.json"
PROGRESS_FILE="location_stats_progress.txt"
CHECKPOINT_FILE="location_stats_checkpoint.json"

# Date paramétrable pour begin_at (argument ligne de commande, sinon défaut 2025-01-01)
BEGIN_AT="${1:-2025-01-01}"

# Vos credentials
CLIENT_ID="u-s4t2af-23f031abd5ab1c7afcd6b43148ddd70b2ae20692602fb8c142f94fabb55b5373"
CLIENT_SECRET="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"

# ========================
# Gestion des signaux (Ctrl+C, etc.)
# ========================
cleanup_and_save() {
    echo ""
    echo "🛑 Interruption détectée. Sauvegarde des données actuelles..."
    
    if [[ -f "$CHECKPOINT_FILE" ]]; then
        cp "$CHECKPOINT_FILE" "$OUTPUT_FILE"
        processed_count=$(jq 'length' "$OUTPUT_FILE" 2>/dev/null || echo "0")
        echo "💾 Données sauvegardées dans '$OUTPUT_FILE' ($processed_count entrées)"
        echo "📄 Fichier de progression sauvé dans '$PROGRESS_FILE'"
        echo ""
        echo "🔄 Pour reprendre là où vous vous êtes arrêté, relancez le script."
        echo "   Les utilisateurs déjà traités seront automatiquement ignorés."
    else
        echo "❌ Aucune donnée à sauvegarder"
    fi
    
    exit 130
}

# Capturer les signaux d'interruption
trap cleanup_and_save SIGINT SIGTERM

# ========================
# Vérification dépendances
# ========================
command -v curl >/dev/null 2>&1 || { echo "Erreur : curl est requis mais non installé."; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Erreur : jq est requis mais non installé."; exit 1; }

# Vérification du fichier d'entrée
if [[ ! -f "$INPUT_FILE" ]]; then
    echo "Erreur : Fichier d'entrée '$INPUT_FILE' non trouvé."
    exit 1
fi

# ========================
# Récupérer le token
# ========================
echo "Obtention du token d'accès depuis l'API 42..."
token_response=$(curl -s -X POST "https://api.intra.42.fr/oauth/token" \
    -u "$CLIENT_ID:$CLIENT_SECRET" \
    -d "grant_type=client_credentials&scope=public projects profile tig elearning forum" \
    -w "\nHTTP_STATUS:%{http_code}")

http_code=$(echo "$token_response" | grep "HTTP_STATUS:" | cut -d: -f2)
body=$(echo "$token_response" | sed '/HTTP_STATUS:/d')

if [[ "$http_code" -ne 200 ]]; then
    echo "Erreur : impossible d'obtenir le token (HTTP $http_code)"
    exit 1
fi

ACCESS_TOKEN=$(echo "$body" | jq -r '.access_token')
if [[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]]; then
    echo "Erreur : impossible d'obtenir le token d'accès"
    exit 1
fi

echo "✅ Token obtenu avec succès."

# ========================
# Initialisation et récupération
# ========================
TOTAL_USERS=$(jq 'length' "$INPUT_FILE" 2>/dev/null)
if [[ -z "$TOTAL_USERS" || "$TOTAL_USERS" == "0" ]]; then
    echo "Erreur : Aucun utilisateur trouvé dans '$INPUT_FILE'"
    exit 1
fi

# Vérifier s'il y a une session précédente à reprendre
PROCESSED_USERS=()
START_INDEX=0

if [[ -f "$CHECKPOINT_FILE" && -f "$PROGRESS_FILE" ]]; then
    echo "🔄 Session précédente détectée."
    
    # Lire les utilisateurs déjà traités
    if [[ -s "$PROGRESS_FILE" ]]; then
        mapfile -t PROCESSED_USERS < "$PROGRESS_FILE"
        START_INDEX=${#PROCESSED_USERS[@]}
        echo "📊 $START_INDEX utilisateurs déjà traités. Reprise à partir de l'utilisateur $((START_INDEX + 1))."
    fi
    
    existing_count=$(jq 'length' "$CHECKPOINT_FILE" 2>/dev/null || echo "0")
    echo "💾 Données existantes : $existing_count entrées"
else
    echo "🆕 Nouvelle session."
    echo "[]" > "$CHECKPOINT_FILE"
    echo "" > "$PROGRESS_FILE"
fi

echo "📊 Total à traiter : $TOTAL_USERS utilisateurs"
echo "📅 Période à partir de : $BEGIN_AT"
echo "💡 Appuyez sur Ctrl+C à tout moment pour sauvegarder et arrêter"

# ========================
# Fonction pour récupérer les stats de localisation
# ========================
fetch_user_stats() {
    local user_id=$1
    local url="$BASE_URL/$user_id/locations_stats?begin_at=$BEGIN_AT"
    
    response=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" "$url" \
        -w "\nHTTP_STATUS:%{http_code}" \
        --connect-timeout 30 \
        --max-time 60)
    
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [[ "$http_code" -ne 200 ]]; then
        error_entry=$(jq -n --arg id "$user_id" --arg error "HTTP $http_code" \
            '{user_id: $id, error: $error, stats: null, timestamp: now | strftime("%Y-%m-%d %H:%M:%S")}')
        echo "$error_entry"
        return 1
    fi
    
    if ! echo "$body" | jq . >/dev/null 2>&1; then
        error_entry=$(jq -n --arg id "$user_id" --arg error "Invalid JSON" \
            '{user_id: $id, error: $error, stats: null, timestamp: now | strftime("%Y-%m-%d %H:%M:%S")}')
        echo "$error_entry"
        return 1
    fi
    
    stat_entry=$(echo "$body" | jq --arg id "$user_id" \
        '{user_id: $id, stats: ., error: null, timestamp: now | strftime("%Y-%m-%d %H:%M:%S")}')
    echo "$stat_entry"
    return 0
}

# ========================
# Fonction pour ajouter une entrée et sauvegarder
# ========================
add_and_save_entry() {
    local entry="$1"
    local user_id="$2"
    
    # Ajouter l'entrée au fichier checkpoint
    local temp_file="/tmp/temp_checkpoint_$$.json"
    jq --argjson new_entry "$entry" '. + [$new_entry]' "$CHECKPOINT_FILE" > "$temp_file" 2>/dev/null
    
    if [[ $? -eq 0 ]]; then
        mv "$temp_file" "$CHECKPOINT_FILE"
        
        # Mettre à jour le fichier de progression
        echo "$user_id" >> "$PROGRESS_FILE"
        
        # Copier vers le fichier de sortie (sauvegarde continue)
        cp "$CHECKPOINT_FILE" "$OUTPUT_FILE"
    else
        echo "⚠️  Erreur lors de la sauvegarde pour l'utilisateur $user_id"
        rm -f "$temp_file"
    fi
}

# ========================
# Traitement des utilisateurs
# ========================
echo ""
echo "🚀 Début du traitement..."

COUNTER=$START_INDEX
SUCCESS_COUNT=$(jq '[.[] | select(.error == null)] | length' "$CHECKPOINT_FILE" 2>/dev/null || echo "0")
ERROR_COUNT=$(jq '[.[] | select(.error != null)] | length' "$CHECKPOINT_FILE" 2>/dev/null || echo "0")

for ((i=START_INDEX; i<TOTAL_USERS; i++)); do
    # Extraire l'utilisateur actuel
    user_json=$(jq -c ".[$i]" "$INPUT_FILE")
    user_id=$(echo "$user_json" | jq -r '.id')
    
    if [[ -z "$user_id" || "$user_id" == "null" ]]; then
        echo "⚠️  ID utilisateur manquant à l'index $i, skipping..."
        continue
    fi
    
    # Vérifier si déjà traité (au cas où)
    if printf '%s\n' "${PROCESSED_USERS[@]}" | grep -q "^$user_id$"; then
        echo "⏭️  Utilisateur $user_id déjà traité, skipping..."
        continue
    fi
    
    # Récupérer les stats
    echo -n "🔄 Traitement utilisateur $((i+1))/$TOTAL_USERS ($user_id)... "
    stat_entry=$(fetch_user_stats "$user_id")
    fetch_result=$?
    
    # Sauvegarder immédiatement
    add_and_save_entry "$stat_entry" "$user_id"
    
    COUNTER=$((COUNTER + 1))
    if [[ $fetch_result -eq 0 ]]; then
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        echo "✅ Succès"
    else
        ERROR_COUNT=$((ERROR_COUNT + 1))
        echo "❌ Erreur"
    fi
    
    # Affichage de progression toutes les 10 entrées
    if [[ $((COUNTER % 10)) -eq 0 ]]; then
        echo "📈 Progression: $COUNTER/$TOTAL_USERS (✅ $SUCCESS_COUNT succès, ❌ $ERROR_COUNT erreurs)"
    fi
    
    # Pause pour respecter les limites de l'API
    sleep 0.125
done

# ========================
# Finalisation
# ========================
echo ""
echo "🎉 Traitement terminé avec succès !"

# Statistiques finales
final_count=$(jq 'length' "$OUTPUT_FILE" 2>/dev/null || echo "0")
final_success=$(jq '[.[] | select(.error == null)] | length' "$OUTPUT_FILE" 2>/dev/null || echo "0")
final_errors=$(jq '[.[] | select(.error != null)] | length' "$OUTPUT_FILE" 2>/dev/null || echo "0")

echo "✅ Toutes les stats enregistrées dans '$OUTPUT_FILE'"
echo "📊 Statistiques finales :"
echo "   - Total d'entrées : $final_count"
echo "   - Succès : $final_success"
echo "   - Erreurs : $final_errors"

if [[ -f "$OUTPUT_FILE" ]]; then
    file_size=$(du -h "$OUTPUT_FILE" | cut -f1)
    echo "📁 Taille du fichier : $file_size"
fi

# Nettoyer les fichiers de progression (session terminée)
rm -f "$PROGRESS_FILE" "$CHECKPOINT_FILE"
echo "🧹 Fichiers temporaires nettoyés."