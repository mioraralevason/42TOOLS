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
