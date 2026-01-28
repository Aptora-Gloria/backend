-- Adım A: Duplicate kontrolü
-- Aynı repo+language kaç kez var?
SELECT repository_id, language_name, COUNT(*) AS cnt
FROM github_repository_languages
GROUP BY repository_id, language_name
HAVING COUNT(*) > 1
ORDER BY cnt DESC;
