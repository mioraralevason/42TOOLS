#!/bin/bash

# ========================
# Configuration
# ========================
CAMPUS_ID="65"
OUTPUT_FILE="DATA/$CAMPUS_ID/LOCATIONS/campus${CAMPUS_ID}_locations.json"
USER_IDS_FILE="DATA/$CAMPUS_ID/LOCATIONS/campus${CAMPUS_ID}_user_ids.txt"
PAGE_SIZE=100
BEGIN_AT="2024-02-01T00:00:00Z"
END_AT="2025-01-01T00:00:00Z"

CLIENT_ID="u-s4t2af-23f031abd5ab1c7afcd6b43148ddd70b2ae20692602fb8c142f94fabb55b5373"
CLIENT_SECRET="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"

# ========================
# Dépendances
# ========================
command -v curl >/dev/null 2>&1 || { echo "Erreur: curl non installé"; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Erreur: jq non installé"; exit 1; }

# ========================
# Préparer dossier et checkpoint
# ========================
mkdir -p "$(dirname "$OUTPUT_FILE")"
CHECKPOINT_FILE="${OUTPUT_FILE}.checkpoint"
AUTOSAVE_INTERVAL=10  # sauvegarde automatique toutes les 10 pages

if [[ -f "$CHECKPOINT_FILE" ]]; then
    echo "🔄 Reprise depuis le checkpoint..."
    ALL_LOCATIONS=$(cat "$CHECKPOINT_FILE")
    TOTAL_LOCATIONS=$(echo "$ALL_LOCATIONS" | jq 'length')
    PAGE=$((TOTAL_LOCATIONS / PAGE_SIZE + 1))
else
    ALL_LOCATIONS="[]"
    TOTAL_LOCATIONS=0
    PAGE=1
fi

# ========================
# Fonction pour obtenir un token
# ========================
get_token() {
    echo "Obtention du token d'accès..."
    token_response=$(curl -s -X POST "https://api.intra.42.fr/oauth/token" \
        -u "$CLIENT_ID:$CLIENT_SECRET" \
        -d "grant_type=client_credentials&scope=public projects profile tig elearning forum")
    ACCESS_TOKEN=$(echo "$token_response" | jq -r '.access_token')
    [[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]] && { echo "Token invalide"; exit 1; }
}

get_token

# ========================
# Gestion interruption
# ========================
cleanup() {
    echo ""
    echo "🛑 Interruption détectée. Sauvegarde des données..."
    echo "$ALL_LOCATIONS" | jq '.' > "$CHECKPOINT_FILE"
    echo "💾 Checkpoint sauvegardé : $CHECKPOINT_FILE"
    exit 130
}

trap cleanup SIGINT SIGTERM

# ========================
# Pagination et récupération
# ========================
echo "⏱ Début du téléchargement des locations pour le campus $CAMPUS_ID"
START_TIME=$(date +%s)

while true; do
    URL="https://api.intra.42.fr/v2/locations?campus_id=${CAMPUS_ID}&sort=begin_at&range%5Bbegin_at%5D=${BEGIN_AT},${END_AT}&page%5Bsize%5D=${PAGE_SIZE}&page%5Bnumber%5D=${PAGE}"

    response=$(curl -s -w "HTTP_CODE:%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" "$URL")
    http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')

    if [[ "$http_code" -eq 401 ]]; then
        echo "⚠️  Token expiré, régénération..."
        get_token
        continue
    elif [[ "$http_code" -ne 200 ]]; then
        echo "Erreur HTTP $http_code"
        break
    fi

    [[ "$(echo "$response_body" | jq type 2>/dev/null)" != "\"array\"" ]] && { echo "Réponse invalide"; break; }

    count=$(echo "$response_body" | jq 'length')
    [[ "$count" -eq 0 ]] && break

    ALL_LOCATIONS=$(echo "$ALL_LOCATIONS" | jq --argjson new "$response_body" '. + $new')
    TOTAL_LOCATIONS=$((TOTAL_LOCATIONS + count))

    # Sauvegarde checkpoint après chaque page
    echo "$ALL_LOCATIONS" | jq '.' > "$CHECKPOINT_FILE"

    # Sauvegarde automatique toutes les N pages
    if (( PAGE % AUTOSAVE_INTERVAL == 0 )); then
        echo "💾 Sauvegarde automatique après $PAGE pages..."
        echo "$ALL_LOCATIONS" | jq '.' > "$CHECKPOINT_FILE"
    fi

    # Affichage progression avec temps écoulé
    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
    printf "Page %d: %d locations récupérées (Total: %d) | Temps écoulé: %02dh:%02dm:%02ds\n" \
        "$PAGE" "$count" "$TOTAL_LOCATIONS" "$((ELAPSED/3600))" "$(((ELAPSED%3600)/60))" "$((ELAPSED%60))"

    PAGE=$((PAGE + 1))
    sleep 0.2
done

# ========================
# Sauvegarde finale
# ========================
echo "$ALL_LOCATIONS" | jq '.' > "$OUTPUT_FILE"
USER_IDS=$(echo "$ALL_LOCATIONS" | jq -r '[.[].user.id] | unique | .[]')
echo "$USER_IDS" > "$USER_IDS_FILE"

# Supprimer checkpoint
rm -f "$CHECKPOINT_FILE"

END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))
printf "✅ Locations sauvegardées dans %s\n" "$OUTPUT_FILE"
printf "✅ IDs utilisateurs sauvegardés dans %s\n" "$USER_IDS_FILE"
printf "Nombre total de locations: %d\n" "$TOTAL_LOCATIONS"
printf "Nombre d'utilisateurs uniques: %d\n" "$(echo "$USER_IDS" | wc -l)"
printf "⏱ Temps total écoulé: %02dh:%02dm:%02ds\n" "$((TOTAL_TIME/3600))" "$(((TOTAL_TIME%3600)/60))" "$((TOTAL_TIME%60))"
