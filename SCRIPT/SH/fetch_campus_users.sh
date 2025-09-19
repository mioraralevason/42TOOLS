#!/bin/bash

# ========================
# Configuration
# ========================
BASE_URL="https://api.intra.42.fr/v2/campus"
CAMPUS_ID="65"
OUTPUT_DIR="DATA/$CAMPUS_ID/USERS"
OUTPUT_FILE="$OUTPUT_DIR/campus${CAMPUS_ID}_users.json"
CHECKPOINT_FILE="${OUTPUT_FILE}.checkpoint"
PAGE_SIZE=100
PAGE=1
BATCH_SIZE=10
PARALLEL_CALLS=5
REQUEST_DELAY=0.2

CLIENT_ID="u-s4t2af-23f031abd5ab1c7afcd6b43148ddd70b2ae20692602fb8c142f94fabb55b5373"
CLIENT_SECRET="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"

# ========================
# Dépendances
# ========================
command -v curl >/dev/null 2>&1 || { echo "Erreur : curl est requis."; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Erreur : jq est requis."; exit 1; }

# ========================
# Préparer dossier et checkpoint
# ========================
mkdir -p "$OUTPUT_DIR"
if [[ -f "$CHECKPOINT_FILE" ]]; then
    echo "🔄 Reprise depuis le checkpoint..."
    cp "$CHECKPOINT_FILE" "/tmp/merged_temp.json"
else
    echo "[]" > "/tmp/merged_temp.json"
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
    [[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]] && { echo "Erreur : impossible d'obtenir le token."; exit 1; }
}

get_token

# ========================
# Gestion interruption
# ========================
cleanup() {
    echo ""
    echo "🛑 Interruption détectée. Sauvegarde du checkpoint..."
    cp "/tmp/merged_temp.json" "$CHECKPOINT_FILE"
    echo "💾 Checkpoint sauvegardé : $CHECKPOINT_FILE"
    exit 130
}

trap cleanup SIGINT SIGTERM

# ========================
# Pagination batch
# ========================
echo "⏱ Début du téléchargement des utilisateurs pour le campus $CAMPUS_ID"
START_TIME=$(date +%s)
total_users=$(jq 'length' /tmp/merged_temp.json)

while true; do
    declare -a pids
    end_loop=false

    # Lancer batch de pages
    for i in $(seq 0 $((BATCH_SIZE - 1))); do
        p=$((PAGE + i))
        page_file="/tmp/page_${p}.json"
        url="${BASE_URL}/${CAMPUS_ID}/users?page%5Bnumber%5D=${p}&page%5Bsize%5D=${PAGE_SIZE}"

        # Limiter les appels parallèles
        while [ $(jobs -rp | wc -l) -ge $PARALLEL_CALLS ]; do
            sleep 0.05
        done

        (
            response=$(curl -s -w "HTTP_CODE:%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" "$url")
            http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
            response_body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')

            if [[ "$http_code" -eq 401 ]]; then
                echo "⚠️  Token expiré, régénération..."
                get_token
                exit 1
            elif [[ "$http_code" -ne 200 ]]; then
                echo "Erreur HTTP $http_code"
                exit 1
            fi

            echo "$response_body" > "$page_file"
        ) &
        pids+=($!)
    done

    # Attendre tous les fetch du batch
    for pid in "${pids[@]}"; do
        wait $pid
    done

    # Fusionner les pages
    for i in $(seq 0 $((BATCH_SIZE - 1))); do
        page_file="/tmp/page_$((PAGE + i)).json"
        if [[ -f "$page_file" ]]; then
            count=$(jq 'length' "$page_file" 2>/dev/null || echo "0")
            if [[ $count -eq 0 ]]; then
                end_loop=true
                rm "$page_file"
                break
            fi
            temp_merged="/tmp/new_merged_temp.json"
            jq -s '.[0] + .[1]' "/tmp/merged_temp.json" "$page_file" > "$temp_merged"
            mv "$temp_merged" "/tmp/merged_temp.json"
            total_users=$((total_users + count))
            rm "$page_file"
            sleep $REQUEST_DELAY
        fi
    done

    # Sauvegarde checkpoint après chaque batch
    cp "/tmp/merged_temp.json" "$CHECKPOINT_FILE"

    # Affichage temps écoulé
    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
    printf "Batch PAGE %d à %d terminé | Total utilisateurs: %d | Temps écoulé: %02dh:%02dm:%02ds\n" \
        "$PAGE" "$((PAGE + BATCH_SIZE - 1))" "$total_users" "$((ELAPSED/3600))" "$(((ELAPSED%3600)/60))" "$((ELAPSED%60))"

    [[ "$end_loop" = true ]] && break
    PAGE=$((PAGE + BATCH_SIZE))
done

# ========================
# Sauvegarde finale
# ========================
cp "/tmp/merged_temp.json" "$OUTPUT_FILE"
rm -f "$TEMP_FILE" "/tmp/merged_temp.json" "$CHECKPOINT_FILE"

END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))
printf "✅ Export terminé : %s\n" "$OUTPUT_FILE"
printf "📊 Nombre total d'utilisateurs : %d\n" "$total_users"
printf "⏱ Temps total écoulé: %02dh:%02dm:%02ds\n" "$((TOTAL_TIME/3600))" "$(((TOTAL_TIME%3600)/60))" "$((TOTAL_TIME%60))"
