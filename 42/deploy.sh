#!/bin/bash
set -e

# -----------------------------
# Configuration
# -----------------------------
APP_NAME="stage42"
DOCKER_IMAGE="stage42-app"
JAR_NAME="stage-42-0.0.1-SNAPSHOT.jar"
ROOT_DIR="./"           # Répertoire racine du projet (avec pom.xml et Dockerfile)
DOCKERFILE_PATH="Dockerfile"

# -----------------------------
# Build Maven
# -----------------------------
echo "🔨 Build du projet Maven..."
./mvnw clean package -DskipTests

# Vérifier que le JAR a été créé
if [ ! -f "$ROOT_DIR/target/$JAR_NAME" ]; then
    echo "❌ Fichier JAR non trouvé : $ROOT_DIR/target/$JAR_NAME"
    exit 1
fi
echo "✅ Build terminé : $JAR_NAME"

# -----------------------------
# Build Docker
# -----------------------------
echo "🐳 Construction de l'image Docker..."
sudo docker build -t $DOCKER_IMAGE -f $ROOT_DIR/$DOCKERFILE_PATH $ROOT_DIR

# -----------------------------
# Stop & remove ancien container si existant
# -----------------------------
if sudo docker ps -a --format '{{.Names}}' | grep -Eq "^$APP_NAME\$"; then
    echo "🛑 Stop et suppression de l'ancien container..."
    sudo docker stop $APP_NAME
    sudo docker rm $APP_NAME
fi

# -----------------------------
# Run Docker
# -----------------------------
echo "🚀 Lancement du container Docker..."
sudo docker run -d --name $APP_NAME -p 9090:9090 $DOCKER_IMAGE

echo "✅ Container en cours d'exécution : $APP_NAME"
echo "🌐 Accès local : http://localhost:9090"

# -----------------------------
# Instructions pour Render
# -----------------------------
echo "💡 Pour Render :"
echo "   Root Directory : $ROOT_DIR"
echo "   Dockerfile Path : $DOCKERFILE_PATH"
echo "   Variables d'environnement à définir : CLIENT_ID, CLIENT_SECRET, REDIRECT_URI"
echo "   Render utilisera automatiquement \$PORT pour le port exposé"
