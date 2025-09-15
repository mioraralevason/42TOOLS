#!/bin/bash

# ========================
# Configuration
# ========================
BASE_URL="https://api.intra.42.fr/v2/campus"
CAMPUS_ID="65"
OUTPUT_FILE="campus65_users.json"
TEMP_FILE="/tmp/campus_users_temp.json"
PAGE_SIZE=100
PAGE=1

# API credentials
CLIENT_ID="u-s4t2af-23f031abd5ab1c7afcd6b43148ddd70b2ae20692602fb8c142f94fabb55b5373"
CLIENT_SECRET="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"

# ========================
# Vérification dépendances
# ========================
command -v curl >/dev/null 2>&1 || { echo "Erreur : curl est requis."; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Erreur : jq est requis."; exit 1; }

# ========================
# Récupérer le token
# ========================
echo "Obtention du token d'accès..."
token_response=$(curl -s -X POST "https://api.intra.42.fr/oauth/token" \
    -u "$CLIENT_ID:$CLIENT_SECRET" \
    -d "grant_type=client_credentials&scope=public projects profile tig elearning forum" \
    -w "\nHTTP_STATUS:%{http_code}")

# Séparer le body et le code de statut
http_code=$(echo "$token_response" | grep "HTTP_STATUS:" | cut -d: -f2)
body=$(echo "$token_response" | sed '/HTTP_STATUS:/d')

if [[ "$http_code" -ne 200 ]]; then
    echo "Erreur : impossible d'obtenir le token (HTTP $http_code)"
    echo "$body"
    exit 1
fi

ACCESS_TOKEN=$(echo "$body" | jq -r '.access_token')
if [[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]]; then
    echo "Erreur : token d'accès vide ou invalide"
    exit 1
fi

echo "✅ Token obtenu."

# ========================
# Vérifier le campus
# ========================
echo "Vérification du campus $CAMPUS_ID..."
campus_response=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" \
    "$BASE_URL/$CAMPUS_ID" \
    -w "\nHTTP_STATUS:%{http_code}")

http_code=$(echo "$campus_response" | grep "HTTP_STATUS:" | cut -d: -f2)
body=$(echo "$campus_response" | sed '/HTTP_STATUS:/d')

if [[ "$http_code" -ne 200 ]]; then
    echo "Erreur : campus $CAMPUS_ID invalide ou inaccessible (HTTP $http_code)"
    exit 1
fi

campus_name=$(echo "$body" | jq -r '.name')
echo "✅ Campus trouvé : $campus_name"

# ========================
# Initialiser le fichier JSON
# ========================
echo "[]" > "$TEMP_FILE"
total_users=0

# ========================
# Fonction pour récupérer une page
# ========================
fetch_page() {
    local page=$1
    
    echo "Récupération de la page $page..."
    
    # URL encodée manuellement pour éviter les problèmes
    local encoded_url="${BASE_URL}/${CAMPUS_ID}/users?page%5Bnumber%5D=${page}&page%5Bsize%5D=${PAGE_SIZE}"
    
    response=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" \
        "$encoded_url" \
        -w "\nHTTP_STATUS:%{http_code}" \
        --connect-timeout 30 \
        --max-time 60)
    
    # Vérifier si curl a réussi
    curl_exit_code=$?
    if [[ $curl_exit_code -ne 0 ]]; then
        echo "Erreur curl (code $curl_exit_code)"
        return 1
    fi
    
    # Séparer le body et le code de statut
    http_code=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [[ "$http_code" -ne 200 ]]; then
        echo "Erreur HTTP $http_code sur la page $page"
        echo "Response: $body"
        return 1
    fi
    
    # Vérifier si la réponse est valide JSON
    if ! echo "$body" | jq . >/dev/null 2>&1; then
        echo "Erreur : réponse JSON invalide"
        return 1
    fi
    
    # Vérifier si on a des utilisateurs
    user_count=$(echo "$body" | jq 'length')
    if [[ "$user_count" -eq 0 ]]; then
        echo "📌 Aucun utilisateur sur cette page. Fin des données."
        return 1
    fi
    
    # Écrire les données dans un fichier temporaire pour cette page
    page_file="/tmp/page_${page}.json"
    echo "$body" > "$page_file"
    
    # Fusionner avec le fichier principal en utilisant un fichier temporaire
    temp_merged="/tmp/merged_temp.json"
    jq -s '.[0] + .[1]' "$TEMP_FILE" "$page_file" > "$temp_merged"
    mv "$temp_merged" "$TEMP_FILE"
    
    # Nettoyer le fichier de page
    rm "$page_file"
    
    total_users=$((total_users + user_count))
    echo "✅ $user_count utilisateurs ajoutés (Total: $total_users)"
    return 0
}

# ========================
# Boucle sur toutes les pages
# ========================
echo "Début de la récupération des utilisateurs..."

while true; do
    if ! fetch_page $PAGE; then
        break
    fi
    PAGE=$((PAGE + 1))
    
    # Pause pour éviter de surcharger l'API
    sleep 0.5
    
    # Sécurité : éviter une boucle infinie
    if [[ $PAGE -gt 1000 ]]; then
        echo "⚠️  Arrêt de sécurité : plus de 1000 pages traitées"
        break
    fi
done

# ========================
# Sauvegarde finale
# ========================
echo "Sauvegarde des données dans $OUTPUT_FILE..."
cp "$TEMP_FILE" "$OUTPUT_FILE"

# Nettoyer le fichier temporaire
rm "$TEMP_FILE"

# Compter les utilisateurs finaux
final_count=$(jq 'length' "$OUTPUT_FILE" 2>/dev/null || echo "0")
echo "✅ Export terminé : $OUTPUT_FILE"
echo "📊 Nombre total d'utilisateurs : $final_count"

# Afficher quelques statistiques si on a des utilisateurs
if [[ $final_count -gt 0 ]]; then
    echo "📋 Aperçu des données :"
    head_data=$(jq -r '.[0:3] | .[] | "  - " + .login + " (" + .email + ")"' "$OUTPUT_FILE" 2>/dev/null)
    if [[ $? -eq 0 ]]; then
        echo "$head_data"
        if [[ $final_count -gt 3 ]]; then
            echo "  ... et $((final_count - 3)) autres"
        fi
    else
        echo "  (Impossible d'afficher l'aperçu, mais les données sont sauvées)"
    fi
    
    # Afficher la taille du fichier
    file_size=$(du -h "$OUTPUT_FILE" | cut -f1)
    echo "📁 Taille du fichier : $file_size"
else
    echo "⚠️  Aucun utilisateur dans le fichier final"
    echo "Vérification du fichier de sortie:"
    ls -la "$OUTPUT_FILE"
    echo "Contenu:"
    head -5 "$OUTPUT_FILE"
fi