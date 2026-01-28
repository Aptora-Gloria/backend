-- Migration: Deduplicate github_repository_languages
-- Purpose: Remove duplicate entries for (repository_id, language_name) pairs
-- Strategy: Keep the record with the most recent last_synced_at, or highest id if last_synced_at is NULL

-- Step 1: Delete duplicate records, keeping only the most recent one per (repository_id, language_name)
WITH ranked AS (
  SELECT
    id,
    repository_id,
    language_name,
    ROW_NUMBER() OVER (
      PARTITION BY repository_id, language_name
      ORDER BY last_synced_at DESC NULLS LAST, id DESC
    ) AS rn
  FROM github_repository_languages
)
DELETE FROM github_repository_languages
WHERE id IN (
  SELECT id FROM ranked WHERE rn > 1
);

-- Step 2: Verify no duplicates remain (this should return 0 rows)
-- SELECT repository_id, language_name, COUNT(*)
-- FROM github_repository_languages
-- GROUP BY repository_id, language_name
-- HAVING COUNT(*) > 1;
