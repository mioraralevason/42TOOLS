-- Taux de présence par utilisateur basé sur les jours
WITH date_range AS (
    SELECT generate_series('2025-07-01'::date, '2025-08-15'::date, interval '1 day') AS day
)
SELECT 
    u.user_id,
    u.displayname,
    COUNT(DISTINCT s.date_) AS jours_present,                      -- nombre de jours où l'utilisateur est venu
    (SELECT COUNT(*) FROM date_range) AS jours_totaux,             -- nombre total de jours dans la période
    ROUND(COUNT(DISTINCT s.date_)::numeric / (SELECT COUNT(*) FROM date_range) * 100, 2) AS taux_presence
FROM User_ u
LEFT JOIN Stats s
    ON s.user_id = u.user_id
    AND s.date_ BETWEEN '2025-07-01' AND '2025-08-15'
GROUP BY u.user_id, u.displayname
ORDER BY taux_presence DESC;

-- Taux de présence global basé sur les jours
WITH date_range AS (
    SELECT generate_series('2025-07-01'::date, '2025-08-15'::date, interval '1 day') AS day
)
SELECT 
    ROUND(SUM(CASE WHEN s.date_ IS NOT NULL THEN 1 ELSE 0 END)::numeric 
          / ((SELECT COUNT(*) FROM date_range) * 250) * 100, 2) AS taux_presence_global
FROM Stats s
WHERE s.date_ BETWEEN '2025-07-01' AND '2025-08-15';


COPY (
    SELECT 
        u.login,

        -- Depuis 03 mars 2025
        (SELECT taux_presence FROM taux_presence_par_utilisateur('2025-03-03', '2025-10-05') tp WHERE tp.login = u.login) AS taux_depuis_03_mars,

        -- Depuis 3 mois
        (SELECT taux_presence FROM taux_presence_par_utilisateur('2025-07-05', '2025-10-05') tp WHERE tp.login = u.login) AS taux_depuis_3_mois,

        -- Depuis 1 mois
        (SELECT taux_presence FROM taux_presence_par_utilisateur('2025-09-05', '2025-10-05') tp WHERE tp.login = u.login) AS taux_depuis_1_mois

    FROM (
        VALUES 
            ('nravonia'), ('rarahari'), ('ljoelnom'), ('nambrako'), ('jrabenah'),
            ('horandri'), ('mirrakot'), ('andriamr'), ('larakoto'), ('rorandri'),
            ('hratsito'), ('irazafim'), ('ftsironi'), ('sarakoto'), ('landriam'),
            ('candriam'), ('ballain'), ('nrabehar'), ('hariandr'), ('atahiry-'),
            ('frakotoa'), ('nraveloj'), ('grasoani'), ('fharifen'), ('hrasolom'),
            ('miokrako'), ('aravelom'), ('arabeman'), ('marrandr'), ('tefrakot'),
            ('andoandr'), ('srasolom'), ('nrazafim'), ('trazanad'), ('tokyandr'),
            ('braelino'), ('zramahaz'), ('keandria'), ('niaandri'), ('sranaivo'),
            ('jramihaj'), ('tramanan'), ('rrabetsy'), ('frazakar'), ('fidrandr'),
            ('trasamiz'), ('nharivon'), ('fanrazaf'), ('hajrakot'), ('mandriaf')
    ) AS u(login)

    ORDER BY 
        (SELECT taux_presence 
         FROM taux_presence_par_utilisateur('2025-03-03', '2025-10-05') tp 
         WHERE tp.login = u.login) DESC

) TO '/tmp/taux_presence.csv' WITH CSV HEADER;






-- =====================================================
-- FONCTION : Taux de présence par semaine (basé sur durée)
-- =====================================================
CREATE OR REPLACE FUNCTION moyennes_heures_par_semaine_par_utilisateur(
    date_debut DATE,
    date_fin DATE
)
RETURNS TABLE (
    login VARCHAR,
    semaine INTEGER,
    date_semaine DATE,
    heures_total NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.login,
        EXTRACT(WEEK FROM s.date_)::INT AS semaine,
        DATE_TRUNC('week', s.date_)::DATE AS date_semaine,
        ROUND(SUM(EXTRACT(EPOCH FROM s.duration) / 3600), 2) AS heures_total  -- total heures par semaine
    FROM User_ u
    LEFT JOIN Stats s
        ON s.user_id = u.user_id
        AND s.date_ BETWEEN date_debut AND date_fin
    GROUP BY u.login, semaine, date_semaine
    ORDER BY u.login, semaine;
END;
$$ LANGUAGE plpgsql;   





COPY (
    SELECT 
        u.login,
        (SELECT ROUND(AVG(tp.heures_total), 2)
         FROM moyennes_heures_par_semaine_par_utilisateur('2025-03-03', '2025-10-05') tp
         WHERE tp.login = u.login) AS moyenne_heure_depuis_03_mars,
        (SELECT ROUND(AVG(tp.heures_total), 2)
         FROM moyennes_heures_par_semaine_par_utilisateur('2025-07-05', '2025-10-05') tp
         WHERE tp.login = u.login) AS moyenne_heure_depuis_3_mois,
        (SELECT ROUND(AVG(tp.heures_total), 2)
         FROM moyennes_heures_par_semaine_par_utilisateur('2025-09-05', '2025-10-05') tp
         WHERE tp.login = u.login) AS moyenne__heure_depuis_1_mois
    FROM (
        VALUES 
          ('nravonia')
    ) AS u(login)
    ORDER BY moyenne_heure_depuis_03_mars DESC
) TO '/tmp/moyenne_heures_par_semaine.csv' WITH CSV HEADER;





CREATE OR REPLACE FUNCTION moyenne_heures_utilisateur(
    p_login VARCHAR
)
RETURNS TABLE (
    login VARCHAR,
    moyenne_heure_depuis_debut NUMERIC,
    moyenne_heure_depuis_3_mois NUMERIC,
    moyenne_heure_depuis_1_mois NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.login,
        -- Moyenne depuis le début (ici depuis le 03-03-2025, tu peux adapter)
        (SELECT ROUND(AVG(tp.heures_total), 2)
         FROM moyennes_heures_par_semaine_par_utilisateur('2025-03-03', CURRENT_DATE) tp
         WHERE tp.login = u.login) AS moyenne_heure_depuis_debut,
         
        -- Moyenne des 3 derniers mois
        (SELECT ROUND(AVG(tp.heures_total), 2)
         FROM moyennes_heures_par_semaine_par_utilisateur(CURRENT_DATE - INTERVAL '3 months', CURRENT_DATE) tp
         WHERE tp.login = u.login) AS moyenne_heure_depuis_3_mois,
         
        -- Moyenne du dernier mois
        (SELECT ROUND(AVG(tp.heures_total), 2)
         FROM moyennes_heures_par_semaine_par_utilisateur(CURRENT_DATE - INTERVAL '1 month', CURRENT_DATE) tp
         WHERE tp.login = u.login) AS moyenne_heure_depuis_1_mois
    FROM (VALUES (p_login)) AS u(login);
END;
$$ LANGUAGE plpgsql;
-- Exemple d'appel de la fonction
SELECT * FROM moyenne_heures_utilisateur('nravonia');