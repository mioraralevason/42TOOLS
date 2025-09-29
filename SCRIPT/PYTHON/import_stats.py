#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import psycopg2
from psycopg2.extras import execute_values
from psycopg2 import Error
from datetime import date, timedelta

# ===========================
# Configuration
# ===========================
DB_NAME = "e42"
DB_USER = "postgres"
DB_PASSWORD = "Discovery@123456"
DB_HOST = "localhost"
DB_PORT = "5432"
CAMPUS_ID = "65"

# ===========================
# Connexion PostgreSQL
# ===========================
def connect_db():
    try:
        conn = psycopg2.connect(
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD,
            host=DB_HOST,
            port=DB_PORT
        )
        return conn
    except Error as e:
        print(f"❌ Erreur lors de la connexion à PostgreSQL : {e}")
        return None

# ===========================
# Parser pour les durées (format "HH:MM:SS.mmmmmm")
# ===========================
def parse_duration(duration_str):
    if not duration_str:
        return None
    try:
        h, m, s = duration_str.split(":")
        s, micros = (s.split(".") + ["0"])[:2]
        return timedelta(
            hours=int(h),
            minutes=int(m),
            seconds=int(s),
            microseconds=int(micros[:6])
        )
    except Exception as e:
        print(f"⚠️ Erreur lors de la conversion de {duration_str}: {e}")
        return None

# ===========================
# Import Stats uniquement
# ===========================
def import_stats():
    try:
        with open(f"../SH/DATA/{CAMPUS_ID}/LOCATION_STATS/campus{CAMPUS_ID}_location_stats_yesterday.json", "r", encoding="utf-8") as f:
            stats_list = json.load(f)
    except FileNotFoundError:
        print(f"❌ Erreur : campus{CAMPUS_ID}_location_stats_yesterday.json introuvable")
        return
    except json.JSONDecodeError as e:
        print(f"❌ Erreur lors du décodage JSON : {e}")
        return

    conn = connect_db()
    if not conn:
        return

    cur = conn.cursor()
    try:
        stats_data = []
        for entry in stats_list:
            user_id = entry.get("user_id")
            stats = entry.get("stats", {})

            for date_str, duration_str in stats.items():
                parsed_date = date.fromisoformat(date_str) if date_str else None
                parsed_duration = parse_duration(duration_str)
                if parsed_date and parsed_duration:
                    stats_data.append((parsed_date, parsed_duration, user_id))
                else:
                    print(f"⚠️ Données invalides pour user_id {user_id}, date {date_str}: {duration_str}")

        if stats_data:
            execute_values(cur, """
                INSERT INTO Stats (date_, duration, user_id)
                VALUES %s
                ON CONFLICT DO NOTHING
            """, stats_data)
            print(f"✅ Insertion de {len(stats_data)} entrées dans Stats")
        else:
            print("⚠️ Aucune donnée valide à insérer")

        conn.commit()
        print("✅ Import des statistiques terminé avec succès !")
    except Error as e:
        print(f"❌ Erreur lors de l'import des statistiques : {e}")
        conn.rollback()
    finally:
        cur.close()
        conn.close()

# ===========================
# Exécution
# ===========================
if __name__ == "__main__":
    import_stats()
