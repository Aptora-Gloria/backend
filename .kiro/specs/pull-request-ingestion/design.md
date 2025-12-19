# Pull Request Ingestion System Design

## Overview

The Pull Request Ingestion System extends the existing Optimetric GitHub integration to systematically collect, store, and serve comprehensive Pull Request data. The system follows the established hexagonal architecture with clear separation between API, application, and infrastructure layers. It leverages existing authentication, repository management, and GitHub API client infrastructure while introducing new entities and services for Pull Request data management.

The system implements a multi-stage ingestion pipeline that progressively enriches Pull Request data from basic metadata to complete information including change statistics, reviews, and commits. All operations are designed to be idempotent and incremental, supporting reliable data synchronization with the GitHub API.

## Architecture

The system follows the existing hexagonal architecture pattern with these key components:

### API Layer (`com.optimetric.github.api`)
- **PullRequestController**: REST endpoints for Pull Request data access
- **Request/Response DTOs**: Data transfer objects following existing patterns
- **Authentication**: Integration with existing `@AuthenticationPrincipal` security

### Application Layer (`com.optimetric.github.application`)
- **Service Ports**: Business logic interfaces for Pull Request operations
- **Client Ports**: GitHub API client interfaces for data fetching
- **Job Orchestration**: Coordination of multi-stage ingestion pipeline

### Infrastructure Layer (`com.optimetric.github.infrastructure`)
- **Persistence**: JPA entities, repositories, and service adapters
- **HTTP Clients**: GitHub API integration using existing WebClient
- **Job Management**: Sync job tracking and execution

## Components and Interfaces

### Core Entities

#### PullRequestEntity
```java
@Entity
@Table(name = "github_pull_requests")
public class PullRequestEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "github_pr_id", nullable = false, unique = true)
    private Long githubPrId;
    
    @Column(name = "pr_number", nullable = false)
    private Integer prNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "data_level", nullable = false)
    private DataLevel dataLevel; // BASIC, FULL
    
    // Change statistics (nullable until PR_DETAIL job completes)
    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;
    private OffsetDateTime changeStatsyncedAt;
    
    // Standard PR fields
    private String state; // open, closed
    private Boolean draft;
    private String title;
    private String body;
    private String authorLogin;
    private Long authorGithubId;
    private String htmlUrl;
    private String mergeCommitSha;
    
    // Timestamps
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime closedAt;
    private OffsetDateTime mergedAt;
    private OffsetDateTime lastSyncedAt;
    
    // System timestamps
    private OffsetDateTime systemCreatedAt;
    private OffsetDateTime systemUpdatedAt;
}
```

#### PullRequestReviewEntity
```java
@Entity
@Table(name = "github_pull_request_reviews")
public class PullRequestReviewEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "github_review_id", nullable = false, unique = true)
    private Long githubReviewId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequestEntity pullRequest;
    
    private String reviewerLogin;
    private Long reviewerGithubId;
    private String state; // APPROVED, CHANGES_REQUESTED, COMMENTED
    private String body;
    private OffsetDateTime submittedAt;
    private String htmlUrl;
}
```

#### PullRequestCommitEntity
```java
@Entity
@Table(name = "github_pull_request_commits",
       uniqueConstraints = @UniqueConstraint(columnNames = {"github_repo_id", "sha"}))
public class PullRequestCommitEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "github_repo_id", nullable = false)
    private Long githubRepoId;
    
    @Column(name = "sha", nullable = false, length = 40)
    private String sha;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequestEntity pullRequest;
    
    private String message;
    private String authorLogin;
    private String authorName;
    private String authorEmail;
    private OffsetDateTime committedAt;
    private String htmlUrl;
}
```

#### SyncJobEntity
```java
@Entity
@Table(name = "github_sync_jobs")
public class SyncJobEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private SyncJobType jobType; // PR_LIST, PR_DETAIL, PR_REVIEWS, PR_COMMITS
    
    @Column(name = "repo_full_name", nullable = false)
    private String repoFullName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SyncJobStatus status; // PENDING, RUNNING, COMPLETED, FAILED
    
    private String cursor; // For pagination
    private Integer currentPage;
    private Integer attemptCount;
    private String errorMessage;
    private OffsetDateTime nextRunAt;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
}
```

### Service Interfaces

#### PullRequestServicePort
```java
public interface PullRequestServicePort {
    List<PullRequestDto> getPullRequestsForRepository(String login, String repoFullName);
    PullRequestDto getPullRequest(String login, String repoFullName, Integer prNumber);
    List<PullRequestReviewDto> getPullRequestReviews(String login, String repoFullName, Integer prNumber);
    List<PullRequestCommitDto> getPullRequestCommits(String login, String repoFullName, Integer prNumber);
    
    // Ingestion operations
    void initiatePullRequestSync(String login, String repoFullName);
    SyncJobStatus getSyncStatus(String login, String repoFullName);
}
```

#### PullRequestClientPort
```java
public interface PullRequestClientPort {
    List<GitHubPullRequestResponse> getPullRequests(String accessToken, String owner, String repo, int page);
    GitHubPullRequestDetailResponse getPullRequestDetails(String accessToken, String owner, String repo, int prNumber);
    List<GitHubPullRequestReviewResponse> getPullRequestReviews(String accessToken, String owner, String repo, int prNumber);
    List<GitHubPullRequestCommitResponse> getPullRequestCommits(String accessToken, String owner, String repo, int prNumber);
}
```

### Job Orchestration

#### SyncJobServicePort
```java
public interface SyncJobServicePort {
    void executePrListJob(String repoFullName);
    void executePrDetailJob(String repoFullName, Long pullRequestId);
    void executePrReviewsJob(String repoFullName, Long pullRequestId);
    void executePrCommitsJob(String repoFullName, Long pullRequestId);
    
    SyncJobEntity createJob(SyncJobType jobType, String repoFullName);
    void updateJobStatus(Long jobId, SyncJobStatus status, String errorMessage);
    List<SyncJobEntity> getPendingJobs();
}
```

## Data Models

### Enumerations

```java
public enum DataLevel {
    BASIC,  // Only basic PR metadata available
    FULL    // All data including change stats, reviews, and commits
}

public enum SyncJobType {
    PR_LIST,     // Discover and create/update basic PR records
    PR_DETAIL,   // Fetch change statistics for specific PRs
    PR_REVIEWS,  // Fetch reviews for specific PRs
    PR_COMMITS   // Fetch commits for specific PRs
}

public enum SyncJobStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}
```

### Response DTOs

```java
public record PullRequestDto(
    Long id,
    Long githubPrId,
    Integer prNumber,
    String repoFullName,
    DataLevel dataLevel,
    String state,
    Boolean draft,
    String title,
    String authorLogin,
    String htmlUrl,
    ChangeStatsDto changeStats,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    OffsetDateTime lastSyncedAt
) {}

public record ChangeStatsDto(
    Integer additions,
    Integer deletions,
    Integer changedFiles,
    Boolean available,
    OffsetDateTime syncedAt
) {}
```

## Data Flow

### Ingestion Pipeline

1. **PR_LIST Job**
   - Fetches paginated list of PRs from GitHub API
   - Creates/updates PullRequestEntity records with BASIC data level
   - Schedules PR_DETAIL jobs for new/updated PRs

2. **PR_DETAIL Job**
   - Fetches detailed PR information including change statistics
   - Updates PullRequestEntity with additions, deletions, changedFiles
   - Schedules PR_REVIEWS and PR_COMMITS jobs

3. **PR_REVIEWS Job**
   - Fetches all reviews for a specific PR
   - Creates/updates PullRequestReviewEntity records
   - Uses github_review_id for idempotent operations

4. **PR_COMMITS Job**
   - Fetches all commits for a specific PR
   - Creates/updates PullRequestCommitEntity records
   - Uses (github_repo_id, sha) unique constraint for deduplication

5. **Finalization**
   - When all detail jobs complete, updates data_level to FULL
   - Updates last_synced_at timestamps

### Incremental Synchronization

- Compare GitHub PR updated_at with local last_synced_at
- Skip processing if no changes detected
- Resume interrupted jobs using cursor/page information in SyncJobEntity
- Handle rate limiting and API errors with exponential backoff

## Error Handling

### GitHub API Errors
- **Rate Limiting**: Implement exponential backoff and respect rate limit headers
- **Authentication**: Handle token expiration and refresh
- **Not Found**: Handle deleted PRs and repositories gracefully
- **Network Issues**: Retry with circuit breaker pattern

### Database Errors
- **Constraint Violations**: Use ON CONFLICT DO UPDATE for idempotent operations
- **Transaction Management**: Ensure data consistency across related entities
- **Connection Issues**: Implement connection pooling and retry logic

### Job Management
- **Failed Jobs**: Record error details and implement retry logic
- **Stuck Jobs**: Implement timeout and cleanup mechanisms
- **Concurrent Execution**: Prevent duplicate job execution with database locks

## Testing Strategy

The system will use a dual testing approach combining unit tests and property-based tests to ensure comprehensive coverage and correctness.

### Unit Testing Framework
- **JUnit 5** for test structure and lifecycle management
- **Mockito** for mocking external dependencies (GitHub API, database)
- **Spring Boot Test** for integration testing with test containers
- **TestContainers** with PostgreSQL for database integration tests

### Property-Based Testing Framework
- **jqwik** (Java property-based testing library) for generating test cases
- **Minimum 100 iterations** per property test to ensure statistical confidence
- Each property test tagged with explicit reference to design document properties

### Unit Test Coverage
Unit tests will focus on:
- **API Controller Logic**: Request/response handling, authentication, validation
- **Service Layer Business Logic**: Data transformation, validation rules, error handling
- **Repository Operations**: Database queries, constraint handling, transaction management
- **GitHub Client Integration**: API request formatting, response parsing, error handling
- **Job Orchestration**: State transitions, scheduling logic, error recovery

### Property-Based Test Coverage
Property tests will verify universal behaviors across all valid inputs:
- **Idempotent Operations**: Multiple ingestion runs produce identical results
- **Data Integrity**: Relationships between entities remain consistent
- **Incremental Sync**: Only changed data gets updated
- **API Consistency**: Response format remains consistent across different data states

### Test Data Management
- **Test Fixtures**: Predefined GitHub API responses for consistent testing
- **Data Builders**: Fluent builders for creating test entities and DTOs
- **Database Cleanup**: Automatic cleanup between tests using @Transactional rollback
- **Mock GitHub API**: Consistent mock responses for different scenarios

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Ingestion Job Execution Completeness
*For any* repository and job type (PR_LIST, PR_DETAIL, PR_REVIEWS, PR_COMMITS), when the ingestion system executes the job, all discovered data should be stored with appropriate data level indicators and proper entity relationships.
**Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5**

### Property 2: Idempotent Data Processing
*For any* repository processed multiple times, the system should maintain exactly one record per unique identifier (github_pr_id, github_review_id, (github_repo_id, sha)) regardless of how many times the ingestion runs.
**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**

### Property 3: Incremental Synchronization Behavior
*For any* Pull Request with updated_at timestamp newer than last_synced_at, the system should re-fetch and update the data, while PRs with unchanged timestamps should be skipped, and successful processing should update the last_synced_at timestamp.
**Validates: Requirements 3.1, 3.2, 3.3**

### Property 4: Job State Management
*For any* sync job, the system should maintain proper state transitions (PENDING → RUNNING → COMPLETED/FAILED), persist pagination information for resumability, and handle errors by recording details and incrementing attempt counters.
**Validates: Requirements 3.4, 3.5, 5.1, 5.2, 5.3, 5.4, 5.5**

### Property 5: Comprehensive Data Storage
*For any* Pull Request data ingested, all required fields should be stored in appropriate JPA entities with correct data types, proper relationships, and non-null constraints satisfied.
**Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5**

### Property 6: API Response Consistency
*For any* authenticated request for Pull Request data, the response should include all required metadata (data_level, last_synced_at), use natural keys (repo_full_name), and properly indicate data availability through boolean flags.
**Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**

### Property 7: Data Level Transition Accuracy
*For any* Pull Request, the data_level should be BASIC when only basic data exists, and should transition to FULL only when all detail jobs (PR_DETAIL, PR_REVIEWS, PR_COMMITS) have completed successfully.
**Validates: Requirements 7.3, 7.4**

### Property 8: Change Statistics Availability Indicator
*For any* Pull Request response DTO, the changeStats.available field should be true if and only if the entity has non-null additions, deletions, and changed_files values.
**Validates: Requirements 7.1, 7.2, 7.5**