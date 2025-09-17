-- Fonction pour calculer le taux de présence par utilisateur (sans ceux qui ont 0 jour présent)
CREATE OR REPLACE FUNCTION taux_presence_par_utilisateur(start_date DATE, end_date DATE)
RETURNS TABLE(
    user_id VARCHAR(250),
    login VARCHAR(250),
    displayname VARCHAR(250),
    first_name VARCHAR(250),
    last_name VARCHAR(250),
    jours_present INT,
    jours_totaux INT,
    taux_presence NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    WITH date_range AS (
        SELECT generate_series(start_date, end_date, interval '1 day')::date AS day
    )
    SELECT 
        u.user_id,
        u.login,
        u.displayname,
        u.first_name,
        u.last_name,
        COUNT(DISTINCT s.date_)::INT AS jours_present,
        (SELECT COUNT(*)::INT FROM date_range) AS jours_totaux,
        ROUND(
            COUNT(DISTINCT s.date_)::NUMERIC 
            / NULLIF((SELECT COUNT(*)::NUMERIC FROM date_range),0) * 100,
            2
        ) AS taux_presence
    FROM User_ u
    LEFT JOIN Stats s
        ON s.user_id = u.user_id
        AND s.date_ BETWEEN start_date AND end_date
    GROUP BY u.user_id, u.login, u.displayname, u.first_name, u.last_name
    HAVING COUNT(DISTINCT s.date_) > 0
    ORDER BY taux_presence DESC;
END;
$$ LANGUAGE plpgsql;


-- Fonction pour calculer le taux de présence global
CREATE OR REPLACE FUNCTION taux_presence_global(
    start_date DATE,
    end_date DATE,
    capacite_jour INT DEFAULT 250  -- nombre max d'utilisateurs/jour
)
RETURNS NUMERIC AS $$
DECLARE
    result NUMERIC;
BEGIN
    WITH date_range AS (
        SELECT generate_series(start_date, end_date, interval '1 day')::date AS day
    )
    SELECT 
        ROUND(SUM(CASE WHEN s.date_ IS NOT NULL THEN 1 ELSE 0 END)::numeric 
              / ((SELECT COUNT(*) FROM date_range) * capacite_jour) * 100, 2)
    INTO result
    FROM Stats s
    WHERE s.date_ BETWEEN start_date AND end_date;

    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Exemple d'appel de la fonction
SELECT * FROM taux_presence_global('2025-07-01', '2025-08-15', 250);
SELECT * FROM taux_presence_par_utilisateur('2025-07-01', '2025-08-15');