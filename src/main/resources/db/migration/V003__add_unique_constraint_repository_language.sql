-- Migration: Add unique constraint to github_repository_languages
-- Purpose: Prevent duplicate (repository_id, language_name) entries
-- Note: V002 migration must be run first to remove existing duplicates

ALTER TABLE github_repository_languages
ADD CONSTRAINT uk_repo_language UNIQUE (repository_id, language_name);
