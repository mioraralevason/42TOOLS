#!/bin/bash

APP_NAME="stage42"
IMAGE_NAME="stage42-app"
PORT=9090

echo "=== Déploiement de $APP_NAME ==="

# 1. Arrêter l'ancien container s'il existe
if [ $(sudo docker ps -a -q -f name=$APP_NAME) ]; then
    echo "Arrêt de l'ancien container..."
    sudo docker stop $APP_NAME
    sudo docker rm $APP_NAME
fi

# 2. Vérifier si le port est occupé
PID=$(sudo lsof -t -i:$PORT)
if [ ! -z "$PID" ]; then
    echo "Port $PORT occupé par le PID $PID, arrêt du processus..."
    sudo kill -9 $PID
fi

# 3. Build du JAR Maven
echo "Build du projet Maven..."
./mvnw clean package -DskipTests

# 4. Build de l'image Docker
echo "Build de l'image Docker..."
sudo docker build -t $IMAGE_NAME .

# 5. Lancer le container
echo "Lancement du container..."
sudo docker run -d --name $APP_NAME -p $PORT:$PORT $IMAGE_NAME

echo "=== Déploiement terminé ! ==="
echo "Application disponible sur http://localhost:$PORT"
