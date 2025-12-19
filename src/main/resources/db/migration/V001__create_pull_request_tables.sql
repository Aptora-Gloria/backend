-- Migration script for Pull Request Ingestion System tables
-- This script creates the necessary tables for storing GitHub Pull Request data

-- Create github_pull_requests table
CREATE TABLE IF NOT EXISTS github_pull_requests (
    id BIGSERIAL PRIMARY KEY,
    github_pr_id BIGINT NOT NULL UNIQUE,
    pr_number INTEGER NOT NULL,
    repository_id BIGINT NOT NULL,
    data_level VARCHAR(20) NOT NULL CHECK (data_level IN ('BASIC', 'FULL')),
    
    -- Change statistics (nullable until PR_DETAIL job completes)
    additions INTEGER,
    deletions INTEGER,
    changed_files INTEGER,
    change_stats_synced_at TIMESTAMP WITH TIME ZONE,
    
    -- Standard PR fields
    state VARCHAR(20),
    draft BOOLEAN,
    title VARCHAR(500),
    body TEXT,
    author_login VARCHAR(100),
    author_github_id BIGINT,
    html_url VARCHAR(500),
    merge_commit_sha VARCHAR(40),
    
    -- Timestamps from GitHub
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    merged_at TIMESTAMP WITH TIME ZONE,
    last_synced_at TIMESTAMP WITH TIME ZONE,
    
    -- System timestamps
    system_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    system_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_pull_request_repository FOREIGN KEY (repository_id) REFERENCES repositories(id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_github_pull_requests_repository_id ON github_pull_requests(repository_id);
CREATE INDEX IF NOT EXISTS idx_github_pull_requests_state ON github_pull_requests(state);
CREATE INDEX IF NOT EXISTS idx_github_pull_requests_data_level ON github_pull_requests(data_level);
CREATE INDEX IF NOT EXISTS idx_github_pull_requests_created_at ON github_pull_requests(created_at);
CREATE INDEX IF NOT EXISTS idx_github_pull_requests_updated_at ON github_pull_requests(updated_at);
CREATE INDEX IF NOT EXISTS idx_github_pull_requests_last_synced_at ON github_pull_requests(last_synced_at);

-- Create github_pull_request_reviews table
CREATE TABLE IF NOT EXISTS github_pull_request_reviews (
    id BIGSERIAL PRIMARY KEY,
    github_review_id BIGINT NOT NULL UNIQUE,
    pull_request_id BIGINT NOT NULL,
    reviewer_login VARCHAR(100),
    reviewer_github_id BIGINT,
    state VARCHAR(20),
    body TEXT,
    submitted_at TIMESTAMP WITH TIME ZONE,
    html_url VARCHAR(500),
    
    -- System timestamps
    system_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    system_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_pull_request_review_pull_request FOREIGN KEY (pull_request_id) REFERENCES github_pull_requests(id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_github_pull_request_reviews_pull_request_id ON github_pull_request_reviews(pull_request_id);
CREATE INDEX IF NOT EXISTS idx_github_pull_request_reviews_reviewer_login ON github_pull_request_reviews(reviewer_login);
CREATE INDEX IF NOT EXISTS idx_github_pull_request_reviews_state ON github_pull_request_reviews(state);
CREATE INDEX IF NOT EXISTS idx_github_pull_request_reviews_submitted_at ON github_pull_request_reviews(submitted_at);

-- Create github_pull_request_commits table
CREATE TABLE IF NOT EXISTS github_pull_request_commits (
    id BIGSERIAL PRIMARY KEY,
    github_repo_id BIGINT NOT NULL,
    sha VARCHAR(40) NOT NULL,
    pull_request_id BIGINT NOT NULL,
    message TEXT,
    author_login VARCHAR(100),
    author_name VARCHAR(200),
    author_email VARCHAR(200),
    committed_at TIMESTAMP WITH TIME ZONE,
    html_url VARCHAR(500),
    
    -- System timestamps
    system_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    system_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    
    -- Unique constraint for composite key
    CONSTRAINT uk_github_pull_request_commits_repo_sha UNIQUE (github_repo_id, sha),
    
    -- Foreign key constraint
    CONSTRAINT fk_pull_request_commit_pull_request FOREIGN KEY (pull_request_id) REFERENCES github_pull_requests(id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_github_pull_request_commits_pull_request_id ON github_pull_request_commits(pull_request_id);
CREATE INDEX IF NOT EXISTS idx_github_pull_request_commits_author_login ON github_pull_request_commits(author_login);
CREATE INDEX IF NOT EXISTS idx_github_pull_request_commits_committed_at ON github_pull_request_commits(committed_at);
CREATE INDEX IF NOT EXISTS idx_github_pull_request_commits_sha ON github_pull_request_commits(sha);

-- Create github_sync_jobs table
CREATE TABLE IF NOT EXISTS github_sync_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_type VARCHAR(20) NOT NULL CHECK (job_type IN ('PR_LIST', 'PR_DETAIL', 'PR_REVIEWS', 'PR_COMMITS')),
    repo_full_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    cursor VARCHAR(500),
    current_page INTEGER,
    attempt_count INTEGER DEFAULT 0,
    error_message TEXT,
    next_run_at TIMESTAMP WITH TIME ZONE,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    
    -- System timestamps
    system_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    system_updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_github_sync_jobs_status ON github_sync_jobs(status);
CREATE INDEX IF NOT EXISTS idx_github_sync_jobs_job_type ON github_sync_jobs(job_type);
CREATE INDEX IF NOT EXISTS idx_github_sync_jobs_repo_full_name ON github_sync_jobs(repo_full_name);
CREATE INDEX IF NOT EXISTS idx_github_sync_jobs_next_run_at ON github_sync_jobs(next_run_at);
CREATE INDEX IF NOT EXISTS idx_github_sync_jobs_system_created_at ON github_sync_jobs(system_created_at);
CREATE INDEX IF NOT EXISTS idx_github_sync_jobs_job_type_status ON github_sync_jobs(job_type, status);
CREATE INDEX IF NOT EXISTS idx_github_sync_jobs_repo_job_type_status ON github_sync_jobs(repo_full_name, job_type, status);