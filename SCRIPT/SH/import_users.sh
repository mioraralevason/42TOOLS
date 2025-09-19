#!/bin/bash

echo "🚀 Import des users dans PostgreSQL..."
python3 ../PYTHON/import_users.py
if [ $? -eq 0 ]; then
  echo "✅ Import terminé avec succès."
else
  echo "❌ Erreur lors de l'import des users."
fi