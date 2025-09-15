#!/bin/bash

# ========================
# Configuration
# ========================
CAMPUS_ID="65"
OUTPUT_FILE="campus65_locations_yesterday.json"
PAGE_SIZE=100

# Date pour aujourd'hui (15 septembre 2025)
# Note: L'API 42 utilise UTC, donc on ajuste pour le fuseau horaire de Madagascar (UTC+3)
BEGIN_AT="2025-09-14T21:00:00Z"  # 00:00 heure locale Madagascar
END_AT="2025-09-15T20:59:59Z"    # 23:59 heure locale Madagascar

# API credentials (À REMPLACER par vos vraies valeurs)
CLIENT_ID="u-s4t2af-23f031abd5ab1c7afcd6b43148ddd70b2ae20692602fb8c142f94fabb55b5373"
CLIENT_SECRET="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"

# ========================
# Vérification des dépendances
# ========================
command -v curl >/dev/null 2>&1 || { 
    echo "Erreur: curl n'est pas installé" >&2
    exit 1 
}

command -v jq >/dev/null 2>&1 || { 
    echo "Erreur: jq n'est pas installé" >&2
    echo "Installation: sudo apt-get install jq (Ubuntu/Debian) ou brew install jq (macOS)" >&2
    exit 1 
}

# ========================
# Obtention du token d'accès
# ========================
echo "Obtention du token d'accès..."

token_response=$(curl -s -X POST "https://api.intra.42.fr/oauth/token" \
    -u "$CLIENT_ID:$CLIENT_SECRET" \
    -d "grant_type=client_credentials&scope=public")

if [[ $? -ne 0 ]]; then
    echo "Erreur lors de la requête de token" >&2
    exit 1
fi

ACCESS_TOKEN=$(echo "$token_response" | jq -r '.access_token')

if [[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]]; then
    echo "Erreur: Token invalide ou non reçu" >&2
    echo "Réponse de l'API: $token_response" >&2
    exit 1
fi

echo "Token d'accès obtenu avec succès"

# ========================
# Pagination et récupération des données
# ========================
PAGE=1
ALL_LOCATIONS="[]"
TOTAL_LOCATIONS=0

echo "Début de la récupération des locations pour le campus $CAMPUS_ID"
echo "Période: $BEGIN_AT à $END_AT"

while true; do
    echo "Récupération de la page $PAGE..."
    
    URL="https://api.intra.42.fr/v2/locations?campus_id=${CAMPUS_ID}&sort=begin_at&range%5Bbegin_at%5D=${BEGIN_AT},${END_AT}&page%5Bsize%5D=${PAGE_SIZE}&page%5Bnumber%5D=${PAGE}"
    
    echo "DEBUG - URL complète: $URL"
    
    response=$(curl -s -w "HTTP_CODE:%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" "$URL")
    
    # Extraire le code HTTP
    http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')
    
    echo "DEBUG - Code HTTP: $http_code"
    
    if [[ $? -ne 0 ]]; then
        echo "Erreur cURL pour la page $PAGE" >&2
        break
    fi
    
    if [[ "$http_code" -ne 200 ]]; then
        echo "Erreur HTTP $http_code pour la page $PAGE" >&2
        echo "Réponse: $response_body" >&2
        break
    fi
    
    response="$response_body"
    
    # Vérifier si la réponse est un tableau JSON valide
    if ! echo "$response" | jq -e '. | type == "array"' >/dev/null 2>&1; then
        echo "Erreur: Réponse API invalide pour la page $PAGE" >&2
        echo "Réponse: $response" >&2
        break
    fi
    
    count=$(echo "$response" | jq 'length')
    
    if [[ "$count" -eq 0 ]]; then
        echo "Fin des données atteinte (page $PAGE vide)"
        break
    fi
    
    # Fusion des données
    ALL_LOCATIONS=$(echo "$ALL_LOCATIONS" | jq --argjson new "$response" '. + $new')
    TOTAL_LOCATIONS=$((TOTAL_LOCATIONS + count))
    
    echo "Page $PAGE: $count locations récupérées (Total: $TOTAL_LOCATIONS)"
    
    PAGE=$((PAGE + 1))
    
    # Pause pour éviter de surcharger l'API
    sleep 0.5
done

# ========================
# Sauvegarde et extraction des IDs utilisateurs
# ========================
echo "Extraction des IDs utilisateurs..."

# Extraire les IDs uniques des utilisateurs
USER_IDS=$(echo "$ALL_LOCATIONS" | jq -r '[.[].user.id] | unique | .[]')

# Sauvegarder les données complètes
echo "$ALL_LOCATIONS" | jq '.' > "$OUTPUT_FILE"

# Sauvegarder seulement les IDs utilisateurs
USER_IDS_FILE="campus65_user_ids_yesterday.txt"
echo "$USER_IDS" > "$USER_IDS_FILE"

if [[ $? -eq 0 ]]; then
    echo "Données sauvegardées avec succès dans $OUTPUT_FILE"
    echo "IDs utilisateurs sauvegardés dans $USER_IDS_FILE"
else
    echo "Erreur lors de la sauvegarde" >&2
    exit 1
fi

echo ""
echo "=== STATISTIQUES ==="
echo "Nombre total de locations: $TOTAL_LOCATIONS"
echo "Fichier de sortie complet: $OUTPUT_FILE"
echo "Fichier IDs utilisateurs: $USER_IDS_FILE"
echo "Taille du fichier complet: $(ls -lh "$OUTPUT_FILE" | awk '{print $5}' 2>/dev/null || echo 'N/A')"

# Statistiques des IDs utilisateurs
if [[ $TOTAL_LOCATIONS -gt 0 ]]; then
    echo ""
    echo "=== IDs UTILISATEURS ==="
    
    # Compter les utilisateurs uniques
    unique_users_count=$(echo "$USER_IDS" | wc -l)
    echo "Nombre d'utilisateurs uniques: $unique_users_count"
    
    echo ""
    echo "Liste des IDs utilisateurs:"
    echo "$USER_IDS" | while read -r id; do
        echo "  - $id"
    done
fi

echo ""
echo "Script terminé avec succès!"