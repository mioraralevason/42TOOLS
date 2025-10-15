#!/bin/bash

# ========================
# Configuration
# ========================
CAMPUS_ID="65"
USERS_FILE="DATA/$CAMPUS_ID/USERS/campus${CAMPUS_ID}_users.json"
OUTPUT_BASE_DIR="DATA/$CAMPUS_ID/CANDIDATURES"
OUTPUT_FILE="$OUTPUT_BASE_DIR/user_candidatures.json"
PROGRESS_FILE="$OUTPUT_BASE_DIR/progress.txt"
CLIENT_ID="u-s4t2af-23f031abd5ab1c7afcd6b43148ddd70b2ae20692602fb8c142f94fabb55b5373"
CLIENT_SECRET="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"

# ========================
# Dépendances
# ========================
command -v curl >/dev/null 2>&1 || { echo "Erreur: curl non installé"; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Erreur: jq non installé"; exit 1; }

# ========================
# Vérification des fichiers
# ========================
if [[ ! -f "$USERS_FILE" ]]; then
    echo "Erreur: Fichier $USERS_FILE introuvable"
    exit 1
fi

mkdir -p "$OUTPUT_BASE_DIR"
[[ ! -f "$OUTPUT_FILE" ]] && echo "[]" > "$OUTPUT_FILE"

# ========================
# Obtenir un token
# ========================
get_token() {
    echo "Obtention du token d'accès..."
    tmpfile=$(mktemp)
    curl -s -o "$tmpfile" -u "$CLIENT_ID:$CLIENT_SECRET" \
        -d "grant_type=client_credentials&scope=public projects profile tig elearning forum" \
        https://api.intra.42.fr/oauth/token
    ACCESS_TOKEN=$(jq -r '.access_token' "$tmpfile")
    rm "$tmpfile"
    if [[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]]; then
        echo "❌ Erreur: Token invalide."
        exit 1
    fi
    echo "✅ Token obtenu avec succès."
}

get_token

# ========================
# Gestion des signaux
# ========================
trap 'echo "🛑 Interruption détectée. Progression sauvegardée dans $PROGRESS_FILE."; exit 130' SIGINT SIGTERM

# ========================
# Reprise automatique
# ========================
LAST_DONE_ID=$(cat "$PROGRESS_FILE" 2>/dev/null)
USER_IDS=($(jq -r '.[].id' "$USERS_FILE"))

# Si un utilisateur précédent a été traité, on saute jusqu’à celui-ci
START_INDEX=0
if [[ -n "$LAST_DONE_ID" ]]; then
    for i in "${!USER_IDS[@]}"; do
        if [[ "${USER_IDS[$i]}" == "$LAST_DONE_ID" ]]; then
            START_INDEX=$((i + 1))
            break
        fi
    done
    echo "🔁 Reprise après l'utilisateur $LAST_DONE_ID (index $START_INDEX)"
fi

TOTAL_USERS=${#USER_IDS[@]}
echo "Nombre total d'utilisateurs à traiter : $TOTAL_USERS"

# ========================
# Boucle principale
# ========================
START_TIME=$(date +%s)

for ((i=START_INDEX; i<TOTAL_USERS; i++)); do
    USER_ID="${USER_IDS[$i]}"
    echo "⏱ Traitement de l'utilisateur $USER_ID ($((i+1))/$TOTAL_USERS)"

    URL="https://api.intra.42.fr/v2/user_candidatures?filter%5Buser_id%5D=${USER_ID}"

    tmpfile=$(mktemp)
    http_code=$(curl -s -o "$tmpfile" -w "%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" "$URL")
    response_body=$(cat "$tmpfile")
    rm "$tmpfile"

    # Gestion token expiré
    if [[ "$http_code" -eq 401 ]]; then
        echo "⚠️ Token expiré, régénération..."
        get_token
        tmpfile=$(mktemp)
        http_code=$(curl -s -o "$tmpfile" -w "%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" "$URL")
        response_body=$(cat "$tmpfile")
        rm "$tmpfile"
    fi

    if [[ "$http_code" -ne 200 ]]; then
        echo "❌ Erreur HTTP $http_code pour l'utilisateur $USER_ID"
        echo "$USER_ID" > "$PROGRESS_FILE" # Sauvegarde du dernier ID traité
        continue
    fi

    # Vérifier si la réponse est un tableau
    if [[ "$(echo "$response_body" | jq type 2>/dev/null)" != "\"array\"" ]]; then
        echo "⚠️ Réponse invalide pour $USER_ID"
        echo "$USER_ID" > "$PROGRESS_FILE"
        continue
    fi

    COUNT=$(echo "$response_body" | jq 'length')
    if [[ $COUNT -gt 0 ]]; then
        tmp_json=$(mktemp)
        jq --argjson new "$response_body" '. + $new' "$OUTPUT_FILE" > "$tmp_json" && mv "$tmp_json" "$OUTPUT_FILE"
        echo "✅ $COUNT candidatures ajoutées pour $USER_ID"
    else
        echo "ℹ️ Aucune candidature pour $USER_ID"
    fi

    # Sauvegarde de la progression
    echo "$USER_ID" > "$PROGRESS_FILE"

    # Pause anti-rate limit
    sleep 2
done

# ========================
# Résumé
# ========================
END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))
TOTAL_CANDIDATURES=$(jq 'length' "$OUTPUT_FILE")
echo "✅ Toutes les candidatures sauvegardées dans $OUTPUT_FILE"
echo "Nombre total de candidatures : $TOTAL_CANDIDATURES"
printf "⏱ Temps total écoulé: %02dh:%02dm:%02ds\n" "$((TOTAL_TIME/3600))" "$(((TOTAL_TIME%3600)/60))" "$((TOTAL_TIME%60))"
echo "💾 Progression finale enregistrée dans $PROGRESS_FILE"
