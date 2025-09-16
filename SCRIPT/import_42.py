import json
import psycopg2
from psycopg2.extras import execute_values
from psycopg2 import Error
from datetime import datetime, date, timedelta
import dateutil.parser

# ===========================
# Configuration
# ===========================
DB_NAME = "e42"
DB_USER = "postgres"
DB_PASSWORD = "Discovery@123456"
DB_HOST = "localhost"
DB_PORT = "5432"

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
        print(f"Erreur lors de la connexion à PostgreSQL : {e}")
        return None

# ===========================
# Convertir les dates JSON en TIMESTAMPTZ
# ===========================
def parse_date(date_str):
    if date_str:
        try:
            return dateutil.parser.isoparse(date_str)
        except ValueError as e:
            print(f"Erreur lors de la conversion de la date {date_str}: {e}")
            return None
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
            microseconds=int(micros[:6])  # max 6 chiffres pour PostgreSQL
        )
    except Exception as e:
        print(f"Erreur lors de la conversion de {duration_str}: {e}")
        return None


# ===========================
# 1. Import Users + Images
# ===========================
def import_users():
    try:
        with open("campus65_users.json", "r", encoding="utf-8") as f:
            users = json.load(f)
    except FileNotFoundError:
        print("Erreur : campus65_users.json introuvable")
        return
    except json.JSONDecodeError as e:
        print(f"Erreur lors du décodage JSON : {e}")
        return

    conn = connect_db()
    if not conn:
        return

    cur = conn.cursor()
    try:
        for u in users:
            img = u.get("image", {})
            versions = img.get("versions", {})

            # Insérer image
            cur.execute("""
                INSERT INTO Image (link, large_, medium, small, micro)
                VALUES (%s, %s, %s, %s, %s)
                RETURNING image_id
            """, (
                img.get("link"),
                versions.get("large"),
                versions.get("medium"),
                versions.get("small"),
                versions.get("micro")
            ))
            image_id = cur.fetchone()[0]

            # Insérer utilisateur
            cur.execute("""
                INSERT INTO User_ (
                    user_id, email, login, first_name, last_name,
                    usual_full_name, usual_first_name, url, phone,
                    displayname, kind, staff, correction_point,
                    anonymize_date, data_erasure_date, created_at,
                    updated_at, alumnized_at, alumni, active,
                    pool_month, wallet, pool_year, location, image_id
                )
                VALUES (
                    %s, %s, %s, %s, %s,
                    %s, %s, %s, %s,
                    %s, %s, %s, %s,
                    %s, %s, %s,
                    %s, %s, %s, %s,
                    %s, %s, %s, %s, %s
                )
                ON CONFLICT (user_id) DO NOTHING
            """, (
                str(u.get("id")),
                u.get("email"),
                u.get("login"),
                u.get("first_name"),
                u.get("last_name"),
                u.get("usual_full_name"),
                u.get("usual_first_name"),
                u.get("url"),
                u.get("phone"),
                u.get("displayname"),
                u.get("kind"),
                u.get("staff?", False),
                u.get("correction_point", 0),
                parse_date(u.get("anonymize_date")),
                parse_date(u.get("data_erasure_date")),
                parse_date(u.get("created_at")),
                parse_date(u.get("updated_at")),
                parse_date(u.get("alumnized_at")),
                u.get("alumni?", False),
                u.get("active?", True),
                u.get("pool_month"),
                u.get("wallet", 0),
                u.get("pool_year"),
                u.get("location"),
                image_id
            ))

        conn.commit()
        print(f"✅ Import des utilisateurs terminé avec succès ! ({len(users)} utilisateurs insérés)")
    except Error as e:
        print(f"Erreur lors de l'import des utilisateurs : {e}")
        conn.rollback()
    finally:
        cur.close()
        conn.close()

# ===========================
# 2. Import Stats
# ===========================
def import_stats():
    try:
        with open("campus65_location_stats.json", "r", encoding="utf-8") as f:
            stats_list = json.load(f)
    except FileNotFoundError:
        print("Erreur : campus65_location_stats.json introuvable")
        return
    except json.JSONDecodeError as e:
        print(f"Erreur lors du décodage JSON : {e}")
        return

    conn = connect_db()
    if not conn:
        return

    cur = conn.cursor()
    try:
        # Préparer les données pour insertion en masse
        stats_data = []
        for entry in stats_list:
            user_id = entry.get("user_id")
            stats = entry.get("stats", {})

            # Vérifier si user_id existe dans User_
            cur.execute("SELECT user_id FROM User_ WHERE user_id = %s", (user_id,))
            if not cur.fetchone():
                print(f"⚠️ user_id {user_id} non trouvé dans User_, ignoré")
                continue

            for date_str, duration_str in stats.items():
                parsed_date = date.fromisoformat(date_str) if date_str else None
                parsed_duration = parse_duration(duration_str)
                if parsed_date and parsed_duration:
                    stats_data.append((parsed_date, parsed_duration, user_id))
                else:
                    print(f"⚠️ Données invalides pour user_id {user_id}, date {date_str}: {duration_str}")

        if stats_data:
            # Insertion en masse avec execute_values
            execute_values(cur, """
                INSERT INTO Stats (date_, duration, user_id)
                VALUES %s
                ON CONFLICT DO NOTHING
            """, stats_data)
            print(f"✅ Insertion de {len(stats_data)} entrées dans Stats")
        else:
            print("⚠️ Aucune donnée valide à insérer dans Stats")

        conn.commit()
        print(f"✅ Import des statistiques terminé avec succès ! ({len(stats_data)} entrées insérées)")
    except Error as e:
        print(f"Erreur lors de l'import des statistiques : {e}")
        conn.rollback()
    finally:
        cur.close()
        conn.close()

# ===========================
# Exécuter les imports
# ===========================
if __name__ == "__main__":
    import_users()
    import_stats()
    print("✅ Import global terminé avec succès !")