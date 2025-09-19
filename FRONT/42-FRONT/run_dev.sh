#!/bin/bash

# Aller dans le dossier du projet (par défaut celui du script)
cd "$(dirname "$0")"

# Lancer le serveur de développement avec --force
npm run dev --force
