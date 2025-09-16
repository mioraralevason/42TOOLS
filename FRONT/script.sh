#!/bin/bash

# Script pour mettre à jour Node.js et créer un projet React

echo "=== Mise à jour du système ==="
sudo apt update -y
sudo apt upgrade -y

echo "=== Installation de curl si manquant ==="
sudo apt install curl -y

echo "=== Installation de nvm (Node Version Manager) ==="
curl -fsSL https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash

# Recharger le shell pour nvm
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

echo "=== Installation de la dernière version stable de Node.js ==="
nvm install --lts
nvm use --lts
nvm alias default node

echo "=== Vérification des versions installées ==="
node -v
npm -v

echo "=== Création du projet React '42-FRONT' avec Vite ==="
npm create vite@latest 42-FRONT -- --template react

echo "=== Installation des dépendances ==="
cd 42-FRONT
npm install

echo "=== Lancement du serveur de développement ==="
npm run dev
