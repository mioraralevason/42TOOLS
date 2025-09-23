#!/bin/bash

# Aller dans le dossier du projet (par défaut celui du script)
cd "$(dirname "$0")"

# Lancer le serveur de développement
npm run dev
