-- Adım 5: DB Kontrolü - Her Language İçin Tek Satır Olduğunu Doğrula

-- 1. Genel duplicate kontrolü (hiç duplicate olmamalı)
SELECT 
    'Genel Duplicate Kontrolü' AS test_name,
    COUNT(*) AS duplicate_count
FROM (
    SELECT repository_id, language_name, COUNT(*) AS cnt
    FROM github_repository_languages
    GROUP BY repository_id, language_name
    HAVING COUNT(*) > 1
) AS duplicates;

-- 2. Belirli bir repo için kontrol (repo_full_name değiştir)
-- Örnek: 'facebook/react' için
SELECT 
    'Belirli Repo Kontrolü' AS test_name,
    repository_id,
    language_name,
    COUNT(*) AS cnt,
    MAX(bytes) AS max_bytes,
    MAX(last_synced_at) AS last_synced
FROM github_repository_languages
WHERE repository_id = (
    SELECT id FROM repositories WHERE full_name = 'facebook/react'
)
GROUP BY repository_id, language_name
ORDER BY language_name;

-- 3. Tüm repo'lar için özet
SELECT 
    'Tüm Repo Özeti' AS test_name,
    COUNT(DISTINCT repository_id) AS repo_count,
    COUNT(DISTINCT language_name) AS unique_language_count,
    COUNT(*) AS total_language_records
FROM github_repository_languages;

-- 4. Her repo için language sayısı (duplicate olmamalı)
SELECT 
    r.full_name,
    COUNT(DISTINCT l.language_name) AS unique_language_count,
    COUNT(l.id) AS total_records,
    CASE 
        WHEN COUNT(DISTINCT l.language_name) = COUNT(l.id) THEN '✅ OK'
        ELSE '❌ DUPLICATE VAR!'
    END AS status
FROM repositories r
LEFT JOIN github_repository_languages l ON r.id = l.repository_id
GROUP BY r.id, r.full_name
HAVING COUNT(l.id) > 0
ORDER BY r.full_name;
