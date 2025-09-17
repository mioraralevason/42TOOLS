#!/bin/bash

# ========================
# Configuration
# ========================
CAMPUS_ID="65"
OUTPUT_FILE="campus65_locations_yesterday.json"
PAGE_SIZE=100

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
# Token d'accès
# ========================
echo "Obtention du token d'accès..."
token_response=$(curl -s -X POST "https://api.intra.42.fr/oauth/token" \
    -u "$CLIENT_ID:$CLIENT_SECRET" \
    -d "grant_type=client_credentials&scope=public")
ACCESS_TOKEN=$(echo "$token_response" | jq -r '.access_token')
[[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]] && { echo "Token invalide"; exit 1; }

# ========================
# Pagination
# ========================
PAGE=1
ALL_LOCATIONS="[]"
TOTAL_LOCATIONS=0

echo "Récupération des locations pour le campus $CAMPUS_ID du $BEGIN_AT au $END_AT"

while true; do
    URL="https://api.intra.42.fr/v2/locations?campus_id=${CAMPUS_ID}&sort=begin_at&range%5Bbegin_at%5D=${BEGIN_AT},${END_AT}&page%5Bsize%5D=${PAGE_SIZE}&page%5Bnumber%5D=${PAGE}"
    
    response=$(curl -s -w "HTTP_CODE:%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" "$URL")
    http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')
    
    [[ "$http_code" -ne 200 ]] && { echo "Erreur HTTP $http_code"; break; }
    
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
# Sauvegarde
# ========================
echo "$ALL_LOCATIONS" | jq '.' > "$OUTPUT_FILE"

USER_IDS=$(echo "$ALL_LOCATIONS" | jq -r '[.[].user.id] | unique | .[]')
echo "$USER_IDS" > "campus65_user_ids_yesterday.txt"

echo "✅ Locations sauvegardées dans $OUTPUT_FILE"
echo "✅ IDs utilisateurs sauvegardés dans campus65_user_ids_yesterday.txt"
echo "Nombre total de locations: $TOTAL_LOCATIONS"
echo "Nombre d'utilisateurs uniques: $(echo "$USER_IDS" | wc -l)"
