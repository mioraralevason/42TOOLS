#!/bin/bash

# ========================
# Configuration
# ========================
CAMPUS_ID="65"
OUTPUT_DIR="DATA/$CAMPUS_ID/USER_IN_LOG"
OUTPUT_FILE="$OUTPUT_DIR/campus${CAMPUS_ID}_user_in_log.json"
USER_IDS_FILE="$OUTPUT_DIR/campus${CAMPUS_ID}_user_in_log_ids_today.txt"
PAGE_SIZE=100

mkdir -p "$OUTPUT_DIR"

# Date d'aujourd'hui à Madagascar (UTC+3)
BEGIN_AT="$(TZ='Indian/Antananarivo' date -d 'today' +%Y-%m-%d)T00:00:00Z"
END_AT="$(TZ='Indian/Antananarivo' date -d 'today' +%Y-%m-%d)T23:59:59Z"

# API credentials
CLIENT_ID="u-s4t2af-23f031abd5ab1c7afcd6b43148ddd70b2ae20692602fb8c142f94fabb55b5373"
CLIENT_SECRET="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"

# ========================
# Dépendances
# ========================
command -v curl >/dev/null 2>&1 || { echo "Erreur: curl non installé"; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Erreur: jq non installé"; exit 1; }

# ========================
# Gestion interruption
# ========================
cleanup() {
    echo ""
    echo "🛑 Interruption détectée. Sauvegarde..."
    [[ -f "$CHECKPOINT_FILE" ]] && cp "$CHECKPOINT_FILE" "$OUTPUT_FILE"
    exit 130
}
trap cleanup SIGINT SIGTERM

# ========================
# Token d'accès
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
# Pagination et récupération
# ========================
PAGE=1
ALL_LOCATIONS="[]"
TOTAL_LOCATIONS=0
START_TIME=$(date +%s)

echo "Récupération des locations pour le campus $CAMPUS_ID du $BEGIN_AT au $END_AT"

while true; do
    URL="https://api.intra.42.fr/v2/locations?campus_id=${CAMPUS_ID}&sort=begin_at&range%5Bbegin_at%5D=${BEGIN_AT},${END_AT}&page%5Bsize%5D=${PAGE_SIZE}&page%5Bnumber%5D=${PAGE}"

    tmpfile=$(mktemp)
    http_code=$(curl -s -o "$tmpfile" -w "%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" "$URL")
    response_body=$(cat "$tmpfile")
    rm "$tmpfile"

    if [[ "$http_code" -eq 401 ]]; then
        echo "⚠️ Token expiré, régénération..."
        get_token
        continue
    elif [[ "$http_code" -ne 200 ]]; then
        echo "Erreur HTTP $http_code"; break
    fi

    [[ "$(echo "$response_body" | jq type 2>/dev/null)" != "\"array\"" ]] && { echo "Réponse invalide"; break; }

    count=$(echo "$response_body" | jq 'length')
    [[ "$count" -eq 0 ]] && break

    ALL_LOCATIONS=$(echo "$ALL_LOCATIONS" | jq --argjson new "$response_body" '. + $new')
    TOTAL_LOCATIONS=$((TOTAL_LOCATIONS + count))

    echo "Page $PAGE: $count locations récupérées (Total: $TOTAL_LOCATIONS)"

    PAGE=$((PAGE + 1))
    sleep 0.2
done

# ========================
# Filtrage locations actives
# ========================
ACTIVE_LOCATIONS=$(echo "$ALL_LOCATIONS" | jq '[.[] | select(.end_at == null)]')
ACTIVE_COUNT=$(echo "$ACTIVE_LOCATIONS" | jq 'length')

# ========================
# Sauvegarde
# ========================
echo "$ACTIVE_LOCATIONS" | jq '.' > "$OUTPUT_FILE"

USER_IDS=$(echo "$ACTIVE_LOCATIONS" | jq -r '[.[].user.id] | unique | .[]')
echo "$USER_IDS" > "$USER_IDS_FILE"

ELAPSED=$(( $(date +%s) - START_TIME ))
printf "⏱ Temps total écoulé: %02dh:%02dm:%02ds\n" "$((ELAPSED/3600))" "$(((ELAPSED%3600)/60))" "$((ELAPSED%60))"

echo "✅ Locations actives sauvegardées dans $OUTPUT_FILE"
echo "✅ IDs utilisateurs sauvegardés dans $USER_IDS_FILE"
echo "Nombre total de locations actives: $ACTIVE_COUNT"
echo "Nombre d'utilisateurs uniques connectés: $(echo "$USER_IDS" | wc -l)"
