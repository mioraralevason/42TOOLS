#!/bin/bash

echo "=== Installation de Docker sur Lubuntu ==="

# 1. Mettre à jour les paquets
sudo apt update
sudo apt upgrade -y

# 2. Installer les dépendances
sudo apt install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    software-properties-common \
    gnupg \
    lsb-release

# 3. Ajouter la clé GPG officielle de Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 4. Ajouter le repository Docker
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 5. Mettre à jour et installer Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 6. Ajouter l'utilisateur actuel au groupe docker
sudo usermod -aG docker $USER

# 7. Activer et démarrer Docker
sudo systemctl enable docker
sudo systemctl start docker

# 8. Vérification
echo "=== Vérification de l'installation ==="
docker --version
docker compose version

echo "Installation terminée !"
echo "Il est recommandé de fermer la session et de vous reconnecter pour appliquer les droits docker à l'utilisateur."
