# Requirements Document

## Introduction

The Pull Request Ingestion System enables Optimetric to systematically collect, store, and serve GitHub Pull Request data including PR metadata, change statistics, reviews, and commits. The system ensures data integrity through idempotent operations, supports incremental updates, and provides reliable API access for metric calculations.

## Glossary

- **PR_Ingestion_System**: The complete system responsible for collecting and managing GitHub Pull Request data within the existing Optimetric architecture
- **GitHub_API**: GitHub's REST API used to fetch Pull Request data via existing WebClient infrastructure
- **Sync_Job**: A tracked operation that collects specific types of PR data, stored in sync_jobs table
- **Data_Level**: An enumeration indicating completeness of PR data (BASIC, FULL)
- **Idempotent_Operation**: An operation that produces the same result when executed multiple times using PostgreSQL ON CONFLICT strategies
- **Incremental_Sync**: Process of updating only changed data since last synchronization using updated_at timestamps
- **Natural_Key**: Business identifier used for API access (repo_full_name) following existing repository access patterns
- **User_Context**: Authentication context using existing User entity and access token management
- **Repository_Entity**: Existing repository entity that Pull Request data will be associated with

## Requirements

### Requirement 1

**User Story:** As a system administrator, I want to ingest GitHub Pull Request data systematically, so that the system maintains accurate and complete PR information for metric calculations.

#### Acceptance Criteria

1. WHEN the PR_Ingestion_System executes a PR_LIST job using existing WebClient infrastructure, THE system SHALL discover and upsert Pull Request records with BASIC data level
2. WHEN the PR_Ingestion_System executes a PR_DETAIL job with user access token, THE system SHALL fetch change statistics and update the data level accordingly
3. WHEN the PR_Ingestion_System executes a PR_REVIEWS job for authenticated user repositories, THE system SHALL collect all reviews and store them with unique github_review_id
4. WHEN the PR_Ingestion_System executes a PR_COMMITS job for user-accessible repositories, THE system SHALL collect all commits with unique (github_repo_id, sha) combination
5. WHEN all detail jobs complete successfully for a repository owned by the authenticated user, THE PR_Ingestion_System SHALL mark the Pull Request data_level as FULL

### Requirement 2

**User Story:** As a data engineer, I want idempotent data ingestion operations, so that running the same ingestion multiple times does not create duplicate records.

#### Acceptance Criteria

1. WHEN the PR_Ingestion_System processes the same repository 10 times using PostgreSQL ON CONFLICT, THE system SHALL maintain exactly one record per unique Pull Request
2. WHEN the PR_Ingestion_System encounters existing github_review_id values, THE system SHALL update existing records using ON CONFLICT DO UPDATE without creating duplicates
3. WHEN the PR_Ingestion_System processes commits with existing (github_repo_id, sha) combinations, THE system SHALL update existing records using unique constraints without creating duplicates
4. WHEN the PR_Ingestion_System processes Pull Requests with existing github_pr_id values, THE system SHALL update existing records based on updated_at timestamps using JPA merge operations
5. THE PR_Ingestion_System SHALL use PostgreSQL unique constraints and JPA entity annotations to prevent duplicate key violations

### Requirement 3

**User Story:** As a system operator, I want incremental data synchronization, so that only changed Pull Requests are updated to minimize API usage and processing time.

#### Acceptance Criteria

1. WHEN a Pull Request has an updated_at timestamp newer than last_synced_at using OffsetDateTime comparison, THE PR_Ingestion_System SHALL re-fetch and update the Pull Request data
2. WHEN a Pull Request has not changed since last synchronization based on timestamp comparison, THE PR_Ingestion_System SHALL skip processing that Pull Request
3. WHEN the PR_Ingestion_System completes processing a Pull Request, THE system SHALL update the last_synced_at timestamp using @PreUpdate JPA lifecycle methods
4. THE PR_Ingestion_System SHALL maintain cursor or pagination state in sync_jobs table for efficient GitHub API traversal
5. THE PR_Ingestion_System SHALL support resuming interrupted synchronization jobs from the last successful position stored in sync_jobs table

### Requirement 4

**User Story:** As a developer, I want comprehensive Pull Request data storage, so that all necessary information for metric calculations is available.

#### Acceptance Criteria

1. THE PR_Ingestion_System SHALL store Pull Request canonical data in JPA entities including github_pr_id, association with existing RepositoryEntity, state, OffsetDateTime timestamps, and author information
2. WHEN PR_DETAIL job completes, THE PR_Ingestion_System SHALL store change statistics in Pull Request entity including additions, deletions, and changed_files fields
3. THE PR_Ingestion_System SHALL store review data in separate JPA entity including github_review_id, reviewer information, state, and submitted_at OffsetDateTime
4. THE PR_Ingestion_System SHALL store commit data in separate JPA entity including github_repo_id, sha, committed_at OffsetDateTime, and author_login
5. THE PR_Ingestion_System SHALL maintain JPA @ManyToOne and @OneToMany relationships between Pull Requests, reviews, commits, and existing RepositoryEntity

### Requirement 5

**User Story:** As a system administrator, I want comprehensive job tracking, so that I can monitor ingestion progress and troubleshoot failures.

#### Acceptance Criteria

1. WHEN the PR_Ingestion_System starts any sync job, THE system SHALL create a sync_jobs JPA entity record with job_type enum, repo_full_name, status enum, and OffsetDateTime timestamps
2. WHEN a sync job encounters an error, THE PR_Ingestion_System SHALL record the error details in error_message field and increment attempt counter using JPA update operations
3. WHEN a sync job completes successfully, THE PR_Ingestion_System SHALL update the status enum and record completion OffsetDateTime using @PreUpdate lifecycle methods
4. THE PR_Ingestion_System SHALL maintain cursor or page information in sync_jobs entity fields for resumable GitHub API operations
5. THE PR_Ingestion_System SHALL schedule next_run_at OffsetDateTime for recurring synchronization jobs using existing Spring Boot scheduling patterns

### Requirement 6

**User Story:** As an API consumer, I want to access Pull Request data using natural keys, so that I can retrieve PR information without knowing internal database IDs.

#### Acceptance Criteria

1. WHEN an authenticated client requests Pull Requests for a repository using @AuthenticationPrincipal, THE PR_Ingestion_System SHALL return results using repo_full_name following existing RepoController patterns
2. WHEN returning Pull Request data through REST endpoints, THE PR_Ingestion_System SHALL include data_level enum and last_synced_at OffsetDateTime in response DTOs
3. WHEN returning Pull Request data via ResponseEntity, THE PR_Ingestion_System SHALL indicate change statistics availability through changeStats.available boolean field in DTOs
4. THE PR_Ingestion_System SHALL support retrieving individual Pull Requests by repo_full_name and pr_number using existing repository access validation patterns
5. THE PR_Ingestion_System SHALL support retrieving reviews and commits for specific Pull Requests using natural keys with proper user authorization checks

### Requirement 7

**User Story:** As a data consumer, I want reliable data completeness indicators, so that I can determine if Pull Request data is ready for metric calculations.

#### Acceptance Criteria

1. WHEN Pull Request entity includes non-null change statistics fields, THE PR_Ingestion_System SHALL set changeStats.available to true in response DTOs using mapper logic
2. WHEN Pull Request entity has null change statistics fields, THE PR_Ingestion_System SHALL set changeStats.available to false in response DTOs using mapper validation
3. WHEN all detail jobs complete for a Pull Request, THE PR_Ingestion_System SHALL set data_level enum to FULL using JPA entity updates
4. WHEN only basic Pull Request data exists in the entity, THE PR_Ingestion_System SHALL set data_level enum to BASIC following existing entity patterns
5. THE PR_Ingestion_System SHALL provide last_synced_at OffsetDateTime timestamps for each data component in response DTOs to indicate data freshness