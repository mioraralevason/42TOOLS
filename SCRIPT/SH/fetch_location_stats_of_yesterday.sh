#!/bin/bash

# ========================
# Configuration
# ========================
CAMPUS_ID="65"
BASE_URL="https://api.intra.42.fr/v2/users"
INPUT_FILE="DATA/$CAMPUS_ID/LOCATIONS/campus${CAMPUS_ID}_user_ids_yesterday.txt"
OUTPUT_DIR="DATA/$CAMPUS_ID/LOCATION_STATS"
OUTPUT_FILE="$OUTPUT_DIR/campus${CAMPUS_ID}_location_stats_yesterday.json"
PROGRESS_FILE="$OUTPUT_DIR/location_stats_progress_yesterday.txt"
CHECKPOINT_FILE="$OUTPUT_DIR/location_stats_checkpoint_yesterday.json"

mkdir -p "$OUTPUT_DIR"

# Date d’hier et aujourd'hui, format YYYY-MM-DD
BEGIN_AT="${1:-$(date -d '3 Days ago' +%Y-%m-%d)}"
END_AT="${1:-$(date -d 'today' +%Y-%m-%d)}"

# Credentials
CLIENT_ID="u-s4t2af-23f031abd5ab1c7afcd6b43148ddd70b2ae20692602fb8c142f94fabb55b5373"
CLIENT_SECRET="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"

# ========================
# Dépendances
# ========================
command -v curl >/dev/null 2>&1 || { echo "Erreur : curl requis"; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Erreur : jq requis"; exit 1; }
[[ -f "$INPUT_FILE" ]] || { echo "Erreur : '$INPUT_FILE' introuvable"; exit 1; }

# ========================
# Gestion interruption
# ========================
cleanup() {
    echo ""
    echo "🛑 Interruption détectée. Sauvegarde du checkpoint..."
    [[ -f "$CHECKPOINT_FILE" ]] && cp "$CHECKPOINT_FILE" "$OUTPUT_FILE"
    exit 130
}
trap cleanup SIGINT SIGTERM

# ========================
# Fonction obtenir token
# ========================
get_token() {
    token_response=$(curl -s -X POST "https://api.intra.42.fr/oauth/token" \
        -u "$CLIENT_ID:$CLIENT_SECRET" \
        -d "grant_type=client_credentials&scope=public projects profile tig elearning forum")
    ACCESS_TOKEN=$(echo "$token_response" | jq -r '.access_token')
    [[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]] && { echo "Erreur : impossible d'obtenir le token."; exit 1; }
}

get_token

# ========================
# Initialisation
# ========================
mapfile -t USER_IDS < "$INPUT_FILE"
TOTAL_USERS=${#USER_IDS[@]}
[[ $TOTAL_USERS -gt 0 ]] || { echo "Aucun ID trouvé"; exit 1; }

PROCESSED_USERS=()
START_INDEX=0
[[ -f "$CHECKPOINT_FILE" ]] && [[ -f "$PROGRESS_FILE" ]] && mapfile -t PROCESSED_USERS < "$PROGRESS_FILE" && START_INDEX=${#PROCESSED_USERS[@]} || { echo "Nouvelle session"; echo "[]" > "$CHECKPOINT_FILE"; echo "" > "$PROGRESS_FILE"; }

echo "Total utilisateurs : $TOTAL_USERS"
echo "Période : $BEGIN_AT - $END_AT"

# ========================
# Fonction fetch user stats
# ========================
fetch_user_stats() {
    local user_id=$1
    local url="$BASE_URL/$user_id/locations_stats?begin_at=$BEGIN_AT&end_at=$END_AT"
    response=$(curl -s -w "HTTP_CODE:%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" "$url")
    http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')

    if [[ "$http_code" -eq 401 ]]; then
        echo "⚠️  Token expiré pour $user_id, régénération..."
        get_token
        fetch_user_stats "$user_id"
        return
    elif [[ "$http_code" -ne 200 ]]; then
        echo "Erreur HTTP $http_code pour $user_id"
        echo "{}"
        return
    fi

    echo $(jq -n --arg id "$user_id" --argjson stats "$body" '{user_id: $id, stats: $stats}')
}

# ========================
# Fonction ajouter et sauvegarder
# ========================
add_and_save_entry() {
    local entry="$1"
    local user_id="$2"
    local tmp="/tmp/temp_checkpoint_$$.json"
    jq --argjson new_entry "$entry" '. + [$new_entry]' "$CHECKPOINT_FILE" > "$tmp" && mv "$tmp" "$CHECKPOINT_FILE"
    echo "$user_id" >> "$PROGRESS_FILE"
    cp "$CHECKPOINT_FILE" "$OUTPUT_FILE"
}

# ========================
# Traitement par batch
# ========================
BATCH_SIZE=10
RATE_SLEEP=0.2   # 5 req/sec
START_TIME=$(date +%s)

for ((i=START_INDEX; i<TOTAL_USERS; i+=BATCH_SIZE)); do
    echo "🚀 Traitement batch $((i/BATCH_SIZE+1))"
    for ((j=i; j<i+BATCH_SIZE && j<TOTAL_USERS; j++)); do
        user_id="${USER_IDS[$j]}"
        [[ -z "$user_id" ]] && continue
        [[ " ${PROCESSED_USERS[*]} " == *" $user_id "* ]] && continue
        stat_entry=$(fetch_user_stats "$user_id")
        add_and_save_entry "$stat_entry" "$user_id"
        echo "✅ $user_id traité"
        sleep $RATE_SLEEP
    done
    # Affichage temps écoulé
    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
    printf "⏱ Temps écoulé: %02dh:%02dm:%02ds\n" "$((ELAPSED/3600))" "$(((ELAPSED%3600)/60))" "$((ELAPSED%60))"
done

echo "🎉 Traitement terminé"
final_count=$(jq 'length' "$OUTPUT_FILE")
echo "Total entrées : $final_count"

rm -f "$PROGRESS_FILE" "$CHECKPOINT_FILE"
