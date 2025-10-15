#!/bin/bash

# ========================
# Configuration
# ========================
CAMPUS_ID="65"
BASE_URL="https://api.intra.42.fr/v2/users"
INPUT_FILE="DATA/$CAMPUS_ID/USERS/campus${CAMPUS_ID}_users.json"
OUTPUT_DIR="DATA/$CAMPUS_ID/LOCATION_STATS"
OUTPUT_FILE="$OUTPUT_DIR/campus${CAMPUS_ID}_location_stats.json"
PROGRESS_FILE="$OUTPUT_DIR/location_stats_progress.txt"
CHECKPOINT_FILE="$OUTPUT_DIR/location_stats_checkpoint.json"
LOG_FILE="$OUTPUT_DIR/location_stats.log"

mkdir -p "$OUTPUT_DIR"

# Date paramétrable (arg1 sinon défaut)
BEGIN_AT="${1:-2024-02-19}"


# Credentials (⚠️ ne pas publier en clair en production)
CLIENT_ID="u-s4t2af-23f031abd5ab1c7afcd6b43148ddd70b2ae20692602fb8c142f94fabb55b5373"
CLIENT_SECRET="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"

# ========================
# Dépendances
# ========================
command -v curl >/dev/null 2>&1 || { echo "Erreur : curl requis"; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Erreur : jq requis"; exit 1; }
[[ -f "$INPUT_FILE" ]] || { echo "Erreur : Fichier '$INPUT_FILE' introuvable"; exit 1; }

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
# Token
# ========================
get_token() {
    token_response=$(curl -s -X POST "https://api.intra.42.fr/oauth/token" \
        -u "$CLIENT_ID:$CLIENT_SECRET" \
        -d "grant_type=client_credentials&scope=public projects profile tig elearning forum")

    ACCESS_TOKEN=$(echo "$token_response" | jq -r '.access_token')
    [[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]] && {
        echo "Erreur : impossible d'obtenir le token." | tee -a "$LOG_FILE"
        exit 1
    }
}
get_token

# ========================
# Initialisation
# ========================
TOTAL_USERS=$(jq 'length' "$INPUT_FILE")
[[ "$TOTAL_USERS" -gt 0 ]] || { echo "Aucun utilisateur à traiter"; exit 1; }

PROCESSED_USERS=()
START_INDEX=0
if [[ -f "$CHECKPOINT_FILE" && -f "$PROGRESS_FILE" ]]; then
    mapfile -t PROCESSED_USERS < "$PROGRESS_FILE"
    START_INDEX=${#PROCESSED_USERS[@]}
else
    echo "Nouvelle session"
    echo "[]" > "$CHECKPOINT_FILE"
    echo "" > "$PROGRESS_FILE"
fi

echo "📊 Total utilisateurs : $TOTAL_USERS"
echo "📅 Période : $BEGIN_AT → $END_AT"
echo "💡 Appuyez sur Ctrl+C pour sauvegarder et arrêter"

# ========================
# Fonction fetch stats avec retry
# ========================
fetch_user_stats() {
    local user_id=$1
    local url="$BASE_URL/$user_id/locations_stats?begin_at=$BEGIN_AT"
    local retries=3
    local delay=5

    for (( attempt=1; attempt<=retries; attempt++ )); do
        response=$(curl -s --max-time 20 -H "Authorization: Bearer $ACCESS_TOKEN" "$url" -w "\nHTTP_CODE:%{http_code}")
        http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
        body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//' | tr -d '\r')

        if [[ "$http_code" -eq 200 ]]; then
            # Vérifier si le corps est un JSON valide
            if echo "$body" | jq empty >/dev/null 2>&1; then
                echo "$(jq -n --arg id "$user_id" --argjson stats "$body" '{user_id: $id, stats: $stats}')"
            else
                echo "⚠️ Corps invalide pour $user_id, remplacé par objet vide" | tee -a "$LOG_FILE"
                echo "$(jq -n --arg id "$user_id" '{user_id: $id, stats: []}')"
            fi
            return
        elif [[ "$http_code" -eq 401 ]]; then
            echo "⚠️ Token expiré pour $user_id, régénération..." | tee -a "$LOG_FILE"
            get_token
        elif [[ "$http_code" -eq 000 ]]; then
            echo "⚠️ Timeout réseau pour $user_id (tentative $attempt/$retries)" | tee -a "$LOG_FILE"
        else
            echo "❌ Erreur HTTP $http_code pour $user_id (tentative $attempt/$retries)" | tee -a "$LOG_FILE"
        fi

        sleep $delay
    done

    echo "$(jq -n --arg id "$user_id" '{user_id: $id, stats: []}')"
}

# ========================
# Sauvegarde progressive
# ========================
add_and_save_entry() {
    local entry="$1"
    local user_id="$2"
    local tmp="/tmp/temp_checkpoint_$$.json"

    # Validation du JSON avant sauvegarde
    if echo "$entry" | jq empty >/dev/null 2>&1; then
        jq --argjson new_entry "$entry" '. + [$new_entry]' "$CHECKPOINT_FILE" > "$tmp" && mv "$tmp" "$CHECKPOINT_FILE"
        echo "$user_id" >> "$PROGRESS_FILE"
        cp "$CHECKPOINT_FILE" "$OUTPUT_FILE"
    else
        echo "⚠️ Entrée invalide ignorée pour $user_id" | tee -a "$LOG_FILE"
    fi
}

# ========================
# Traitement principal
# ========================
RATE_SLEEP=0.125
START_TIME=$(date +%s)
COUNTER=$START_INDEX

for ((i=START_INDEX; i<TOTAL_USERS; i++)); do
    user_json=$(jq -c ".[$i]" "$INPUT_FILE")
    user_id=$(echo "$user_json" | jq -r '.id')
    [[ -z "$user_id" || "$user_id" == "null" ]] && continue
    [[ " ${PROCESSED_USERS[*]} " == *" $user_id "* ]] && continue

    stat_entry=$(fetch_user_stats "$user_id")
    add_and_save_entry "$stat_entry" "$user_id"

    COUNTER=$((COUNTER + 1))
    PERCENT=$((COUNTER * 100 / TOTAL_USERS))

    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
    if (( COUNTER > 0 )); then
        AVG_TIME=$((ELAPSED / COUNTER))
        REMAINING=$(( (TOTAL_USERS - COUNTER) * AVG_TIME ))
    else
        REMAINING=0
    fi

    printf "✅ User %s traité (%d/%d, %d%%) | ETA ~%02dh:%02dm:%02ds\n" \
        "$user_id" "$COUNTER" "$TOTAL_USERS" "$PERCENT" \
        "$((REMAINING/3600))" "$(((REMAINING%3600)/60))" "$((REMAINING%60))"

    sleep $RATE_SLEEP
done

echo "🎉 Traitement terminé"
rm -f "$PROGRESS_FILE" "$CHECKPOINT_FILE"
