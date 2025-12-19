-- Rollback script for Pull Request Ingestion System tables
-- This script removes the tables created for GitHub Pull Request data storage

-- Drop tables in reverse order of creation (due to foreign key constraints)
DROP TABLE IF EXISTS github_sync_jobs;
DROP TABLE IF EXISTS github_pull_request_commits;
DROP TABLE IF EXISTS github_pull_request_reviews;
DROP TABLE IF EXISTS github_pull_requests;