#!/bin/bash

echo "🚀 Import des stats dans PostgreSQL..."
python3 ../PYTHON/import_stats.py
if [ $? -eq 0 ]; then
  echo "✅ Import terminé avec succès."
else
  echo "❌ Erreur lors de l'import des stats."
fi