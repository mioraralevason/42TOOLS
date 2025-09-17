#!/bin/bash
set -e

echo "🔄 Mise à jour de l'environnement Node.js et dépendances..."

# Variables
NVM_DIR="$HOME/.nvm"
PROJECT_DIR="$(pwd)"

# Vérif installation NVM
if [ ! -d "$NVM_DIR" ]; then
  echo "📥 Installation de NVM ..."
  curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/master/install.sh | bash
fi

# Charger NVM
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
[ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"

# Installer dernière version de Node.js
LATEST_NODE=$(nvm ls-remote --lts | tail -1 | awk '{print $1}')
CURRENT_NODE=$(node -v 2>/dev/null || echo "none")

if [ "$CURRENT_NODE" != "$LATEST_NODE" ]; then
  echo "📥 Installation de Node.js $LATEST_NODE ..."
  nvm install --lts
  nvm alias default node
else
  echo "✅ Node.js déjà à jour ($CURRENT_NODE)"
fi

# Aller dans le dossier projet
cd "$PROJECT_DIR"

# Vérifier présence de package.json
if [ ! -f "package.json" ]; then
  echo "⚠️ Aucun package.json trouvé. Tu dois initialiser un projet avec : npm init -y"
  exit 1
fi

echo "📦 Vérification des dépendances..."

# Installer Vite et plugin React si manquants
if ! npm list vite >/dev/null 2>&1; then
  echo "📥 Installation de Vite..."
  npm install vite --save-dev
fi

if ! npm list @vitejs/plugin-react >/dev/null 2>&1; then
  echo "📥 Installation plugin React pour Vite..."
  npm install @vitejs/plugin-react --save-dev
fi

# Installer Font Awesome si manquant
if ! npm list @fortawesome/react-fontawesome >/dev/null 2>&1; then
  echo "📥 Installation Font Awesome..."
  npm install @fortawesome/fontawesome-svg-core @fortawesome/free-solid-svg-icons @fortawesome/react-fontawesome
fi

# Installer React Router DOM si manquant
if ! npm list react-router-dom >/dev/null 2>&1; then
  echo "📥 Installation React Router DOM..."
  npm install react-router-dom
fi

# Installer Chart.js et react-chartjs-2 si manquant
if ! npm list chart.js >/dev/null 2>&1 || ! npm list react-chartjs-2 >/dev/null 2>&1; then
  echo "📥 Installation Chart.js et react-chartjs-2..."
  npm install chart.js react-chartjs-2
fi

# Ajouter node_modules/.bin au PATH si absent
if ! grep -q 'node_modules/.bin' ~/.bashrc; then
  echo 'export PATH=$PATH:./node_modules/.bin' >> ~/.bashrc
  echo "✅ PATH mis à jour (./node_modules/.bin ajouté)"
fi

echo "🎉 Environnement prêt ! Tu peux lancer ton projet avec :"
echo "   npm run dev"
