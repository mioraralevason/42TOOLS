#!/bin/bash

# ========================
# Configuration
# ========================
BASE_URL="https://api.intra.42.fr/v2/users"
INPUT_FILE="campus65_user_ids_yesterday.txt"
OUTPUT_FILE="campus65_location_stats_yesterday.json"
PROGRESS_FILE="location_stats_progress_yesterday.txt"
CHECKPOINT_FILE="location_stats_checkpoint_yesterday.json"

# Date d’hier, format YYYY-MM-DD
BEGIN_AT="${1:-$(date -d 'yesterday' +%Y-%m-%d)}"
END_AT="${1:-$(date -d 'today' +%Y-%m-%d)}"
# Credentials
CLIENT_ID="u-s4t2af-23f031abd5ab1c7afcd6b43148ddd70b2ae20692602fb8c142f94fabb55b5373"
CLIENT_SECRET="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"

# ========================
# Gestion des signaux
# ========================
cleanup_and_save() {
    echo ""
    echo "🛑 Interruption détectée. Sauvegarde..."
    [[ -f "$CHECKPOINT_FILE" ]] && cp "$CHECKPOINT_FILE" "$OUTPUT_FILE"
    exit 130
}
trap cleanup_and_save SIGINT SIGTERM

# ========================
# Dépendances
# ========================
command -v curl >/dev/null 2>&1 || { echo "Erreur : curl requis"; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Erreur : jq requis"; exit 1; }
[[ -f "$INPUT_FILE" ]] || { echo "Erreur : '$INPUT_FILE' introuvable"; exit 1; }

# ========================
# Token
# ========================
token_response=$(curl -s -X POST "https://api.intra.42.fr/oauth/token" \
    -u "$CLIENT_ID:$CLIENT_SECRET" \
    -d "grant_type=client_credentials&scope=public projects profile tig elearning forum")
ACCESS_TOKEN=$(echo "$token_response" | jq -r '.access_token')

# ========================
# Initialisation
# ========================
mapfile -t USER_IDS < "$INPUT_FILE"
TOTAL_USERS=${#USER_IDS[@]}
[[ $TOTAL_USERS -gt 0 ]] || { echo "Aucun ID trouvé"; exit 1; }

PROCESSED_USERS=()
START_INDEX=0
[[ -f "$CHECKPOINT_FILE" ]] && [[ -f "$PROGRESS_FILE" ]] && mapfile -t PROCESSED_USERS < "$PROGRESS_FILE" && START_INDEX=${#PROCESSED_USERS[@]} || { echo "Nouvelle session"; echo "[]" > "$CHECKPOINT_FILE"; echo "" > "$PROGRESS_FILE"; }

echo "Total : $TOTAL_USERS utilisateurs"
echo "Période : $BEGIN_AT - $END_AT"

# ========================
# Fonction fetch
# ========================
fetch_user_stats() {
    local user_id=$1
    local url="$BASE_URL/$user_id/locations_stats?begin_at=$BEGIN_AT&end_at=$END_AT"
    response=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" "$url")
    echo $(jq -n --arg id "$user_id" --argjson stats "$response" '{user_id: $id, stats: $stats}')
}

# ========================
# Ajouter et sauvegarder
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
done

echo "🎉 Traitement terminé"
final_count=$(jq 'length' "$OUTPUT_FILE")
echo "Total entrées : $final_count"
rm -f "$PROGRESS_FILE" "$CHECKPOINT_FILE"
