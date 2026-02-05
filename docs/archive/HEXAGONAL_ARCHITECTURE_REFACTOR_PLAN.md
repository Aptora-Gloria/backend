# Hexagonal Architecture Refactor Plan

## 1) Current Architecture Map

### Top-Level Packages and Responsibilities

- **`com.optimetric.auth`**: Authentication and authorization module
  - `api/`: REST controllers and DTOs
  - `application/`: Port interfaces (AuthServicePort, JwtServicePort, UserServicePort, TokenPort)
  - `infrastructure/`: Adapters, entities, repositories, security config
  - `security/`: JWT authentication filter

- **`com.optimetric.github`**: GitHub integration module
  - `api/`: REST controllers and DTOs (request/response)
  - `application/port/`: Service port interfaces (7 ports)
  - `application/client/`: Client port interfaces (6 ports)
  - `infrastructure/http/`: HTTP client adapters for GitHub API
  - `infrastructure/persistence/`: JPA entities, repositories, service adapters
  - `infrastructure/core/`: Mappers, configs, constants, enums, utilities

- **`com.optimetric.shared`**: Shared cross-cutting concerns
  - `exception/`: Custom exceptions and global exception handler
  - `logging/`: Logging filters and utilities

### Dependency Violations

#### Application → Infrastructure Leaks

1. **`PullRequestServicePort`** imports API DTOs:
   - File: `src/main/java/com/optimetric/github/application/port/PullRequestServicePort.java`
   - Violation: Lines 3-6 import `com.optimetric.github.api.response.*` DTOs
   - Impact: Application layer depends on API layer

2. **`RepoServicePort`** imports infrastructure entities:
   - File: `src/main/java/com/optimetric/github/application/port/RepoServicePort.java`
   - Violation: Line 6 imports `com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity`
   - Line 4 imports `com.optimetric.auth.infrastructure.persistence.entity.OwnerType`
   - Impact: Application layer depends on infrastructure entities

3. **`SyncJobServicePort`** imports infrastructure entities:
   - File: `src/main/java/com/optimetric/github/application/port/SyncJobServicePort.java`
   - Violation: Lines 3-5 import `SyncJobEntity`, `SyncJobStatus`, `SyncJobType` from infrastructure
   - Impact: Application layer depends on infrastructure entities

4. **`AuthServicePort`** imports infrastructure entities:
   - File: `src/main/java/com/optimetric/auth/application/AuthServicePort.java`
   - Violation: Line 5 imports `com.optimetric.auth.infrastructure.persistence.entity.User`
   - Line 6 imports `com.optimetric.auth.api.dto.GitHubUserDto`
   - Impact: Application layer depends on infrastructure and API layers

5. **`PullRequestServicePort`** imports API DTOs:
   - File: `src/main/java/com/optimetric/github/application/port/PullRequestServicePort.java`
   - Violation: Lines 3-6 import response DTOs from API layer
   - Impact: Application layer depends on API layer

#### API → Persistence Entity/Repository Leaks

6. **`PullRequestDto`** imports infrastructure enum:
   - File: `src/main/java/com/optimetric/github/api/response/PullRequestDto.java`
   - Violation: Line 3 imports `com.optimetric.github.infrastructure.persistence.entity.DataLevel`
   - Impact: API layer depends on infrastructure persistence layer

7. **Service Adapters** use JPA repositories directly:
   - File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java`
   - Violation: Lines 34-37 inject JPA repositories directly
   - Impact: Service adapters tightly coupled to JPA

8. **Service Adapters** use entities in business logic:
   - File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java`
   - Violation: Methods like `validateRepositoryAccess()` (line 253) return `RepositoryEntity`
   - Methods like `needsSynchronization()` (line 274) accept `PullRequestEntity`
   - Impact: Business logic operates on infrastructure entities

#### Infrastructure → API DTO Leaks

9. **Mappers** import API DTOs:
   - File: `src/main/java/com/optimetric/github/infrastructure/core/mapper/PullRequestMapper.java`
   - Violation: Lines 3-6 import API response DTOs
   - Impact: Infrastructure mappers depend on API layer

10. **Service Adapters** import API DTOs:
    - File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepoServiceAdapter.java`
    - Violation: Line 12 imports `com.optimetric.github.api.response.RepoDto`
    - Impact: Infrastructure depends on API layer

11. **HTTP Client Adapters** import API DTOs:
    - File: `src/main/java/com/optimetric/github/infrastructure/http/LanguageClientAdapter.java`
    - Violation: Line 3 imports `com.optimetric.github.api.response.LanguageDto`
    - Impact: Infrastructure HTTP layer depends on API layer

12. **Service Adapters** import API request DTOs:
    - File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepoServiceAdapter.java`
    - Violation: Line 11 imports `com.optimetric.github.api.request.RepoInitrequest`
    - Impact: Infrastructure depends on API layer

#### Client Port → Infrastructure DTO Leaks

13. **Client Ports** import infrastructure HTTP DTOs:
    - File: `src/main/java/com/optimetric/github/application/client/PullRequestClientPort.java`
    - Violation: Lines 3-6 import `com.optimetric.github.infrastructure.http.dto.*`
    - Impact: Application client ports depend on infrastructure

14. **AuthServicePort** imports API DTOs:
    - File: `src/main/java/com/optimetric/auth/application/AuthServicePort.java`
    - Violation: Lines 3-4, 6 import API DTOs (`AuthRequest`, `AuthResponse`, `GitHubUserDto`)
    - Impact: Application port depends on API layer

15. **Service Adapters** use Spring annotations in business logic:
    - File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java`
    - Violation: Line 19 `@Transactional`, Line 30 `@Service` annotations
    - Methods contain business logic mixed with infrastructure concerns
    - Impact: Business logic coupled to Spring framework

## 2) Use-Case Inventory

### Authentication Use-Cases

**UC-AUTH-01: User Login**
- Entry Point: `AuthController.login()` (line 21)
- Services: `AuthServicePort.login()`
- External Dependencies: GitHub OAuth API (via `GitHubServicePort`)
- Side Effects: Creates/updates User entity, generates JWT token
- Files: `src/main/java/com/optimetric/auth/api/AuthController.java`, `src/main/java/com/optimetric/auth/infrastructure/persistence/adapter/AuthServiceAdapter.java`

**UC-AUTH-02: Get Current User**
- Entry Point: `AuthController.me()` (line 29)
- Services: `AuthServicePort.getCurrentUser()`
- External Dependencies: JWT token validation
- Side Effects: None (read-only)
- Files: `src/main/java/com/optimetric/auth/api/AuthController.java`

### Repository Use-Cases

**UC-REPO-01: Initialize User Repositories**
- Entry Point: `RepoController.initUserRepos()` (line 28)
- Services: `RepoServicePort.initUserRepos()`
- External Dependencies: GitHub API (via `RepoClientPort`), User DB
- Side Effects: Fetches repos from GitHub, saves new repos to DB
- Files: `src/main/java/com/optimetric/github/api/RepoController.java`, `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepoServiceAdapter.java`

**UC-REPO-02: Query Repositories**
- Entry Point: `RepoController.getRepos()` (line 36)
- Services: `RepoServicePort.getRepos()`
- External Dependencies: Repository DB
- Side Effects: None (read-only)
- Files: `src/main/java/com/optimetric/github/api/RepoController.java`

### Pull Request Use-Cases

**UC-PR-01: Get Pull Requests for Repository**
- Entry Point: `PullRequestController.getPullRequests()` (line 44)
- Services: `PullRequestServicePort.getPullRequestsForRepository()`
- External Dependencies: PullRequest DB, Repository DB, User DB (for access validation)
- Side Effects: None (read-only)
- Files: `src/main/java/com/optimetric/github/api/PullRequestController.java`, `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java`

**UC-PR-02: Get Single Pull Request**
- Entry Point: `PullRequestController.getPullRequest()` (line 65)
- Services: `PullRequestServicePort.getPullRequest()`
- External Dependencies: PullRequest DB, Repository DB, User DB
- Side Effects: None (read-only)
- Files: `src/main/java/com/optimetric/github/api/PullRequestController.java`

**UC-PR-03: Get Pull Request Reviews**
- Entry Point: `PullRequestController.getPullRequestReviews()` (line 87)
- Services: `PullRequestServicePort.getPullRequestReviews()`
- External Dependencies: PullRequestReview DB, PullRequest DB
- Side Effects: None (read-only)
- Files: `src/main/java/com/optimetric/github/api/PullRequestController.java`

**UC-PR-04: Get Pull Request Commits**
- Entry Point: `PullRequestController.getPullRequestCommits()` (line 109)
- Services: `PullRequestServicePort.getPullRequestCommits()`
- External Dependencies: PullRequestCommit DB, PullRequest DB
- Side Effects: None (read-only)
- Files: `src/main/java/com/optimetric/github/api/PullRequestController.java`

**UC-PR-05: Initiate Pull Request Sync**
- Entry Point: `PullRequestController.initiatePullRequestSync()` (line 132)
- Services: `PullRequestServicePort.initiatePullRequestSync()`, `SyncJobServicePort.createJob()`, `SyncJobServicePort.executePrListJob()`
- External Dependencies: SyncJob DB, Repository DB, User DB
- Side Effects: Creates sync job, triggers PR_LIST job execution
- Files: `src/main/java/com/optimetric/github/api/PullRequestController.java`, `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java`

**UC-PR-06: Get Sync Status**
- Entry Point: `PullRequestController.getSyncStatus()` (line 151)
- Services: `PullRequestServicePort.getSyncStatus()`, `SyncJobServicePort.getJobsForRepository()`, `SyncJobServicePort.getLatestJobForRepository()`
- External Dependencies: SyncJob DB, Repository DB
- Side Effects: None (read-only)
- Files: `src/main/java/com/optimetric/github/api/PullRequestController.java`

### Branch Use-Cases

**UC-BRANCH-01: Initialize Repository Branches**
- Entry Point: `BranchController.initBranches()` (line 26)
- Services: `BranchServicePort.initRepoBranches()`
- External Dependencies: GitHub API (via `BranchClientPort`), Branch DB
- Side Effects: Fetches branches from GitHub, saves to DB
- Files: `src/main/java/com/optimetric/github/api/BranchController.java`

**UC-BRANCH-02: Query Branches**
- Entry Point: `BranchController.getBranches()` (line 36)
- Services: `BranchServicePort.getBranchesByFullName()`
- External Dependencies: Branch DB
- Side Effects: None (read-only)
- Files: `src/main/java/com/optimetric/github/api/BranchController.java`

### Commit Use-Cases

**UC-COMMIT-01: Initialize Commits**
- Entry Point: `CommitController.initCommits()` (line 28)
- Services: `CommitServicePort.initCommits()`
- External Dependencies: GitHub API (via `CommitClientPort`), Commit DB
- Side Effects: Fetches commits from GitHub, saves to DB
- Files: `src/main/java/com/optimetric/github/api/CommitController.java`

**UC-COMMIT-02: Query Commits**
- Entry Point: `CommitController.getCommits()` (line 42)
- Services: `CommitServicePort.getCommits()`
- External Dependencies: Commit DB
- Side Effects: None (read-only)
- Files: `src/main/java/com/optimetric/github/api/CommitController.java`

### Language Use-Cases

**UC-LANG-01: Initialize Repository Languages**
- Entry Point: `LanguageController.initLanguages()` (line 20)
- Services: `LanguageServicePort.initRepoLanguages()`
- External Dependencies: GitHub API (via `LanguageClientPort`), Language DB
- Side Effects: Fetches languages from GitHub, saves to DB
- Files: `src/main/java/com/optimetric/github/api/LanguageController.java`

**UC-LANG-02: Get Languages from DB**
- Entry Point: `LanguageController.getLanguagesFromDb()` (line 29)
- Services: `LanguageServicePort.getLanguagesFromDb()`
- External Dependencies: Language DB
- Side Effects: None (read-only)
- Files: `src/main/java/com/optimetric/github/api/LanguageController.java`

**UC-LANG-03: Get Languages for UI**
- Entry Point: `LanguageController.getLanguagesForUi()` (line 37)
- Services: `LanguageServicePort.getLanguagesForUi()`
- External Dependencies: Language DB
- Side Effects: None (read-only, includes aggregation/formatting)
- Files: `src/main/java/com/optimetric/github/api/LanguageController.java`

### Scheduled/Background Jobs

**UC-JOB-01: Execute PR_LIST Job**
- Entry Point: `SyncJobServiceAdapter.executePrListJob()` (line 47)
- Services: `PullRequestClientPort.getPullRequests()`, `PullRequestRepository`, `SyncJobRepository`
- External Dependencies: GitHub API, PullRequest DB, SyncJob DB, Repository DB
- Side Effects: Creates/updates PR entities, creates sync jobs, schedules detail jobs
- Files: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java`

**UC-JOB-02: Execute PR_DETAIL Job**
- Entry Point: `SyncJobServiceAdapter.executePrDetailJob()` (line 142)
- Services: `PullRequestClientPort.getPullRequestDetails()`, `PullRequestRepository`
- External Dependencies: GitHub API, PullRequest DB, SyncJob DB
- Side Effects: Updates PR with change statistics, updates data level
- Files: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java`

**UC-JOB-03: Execute PR_REVIEWS Job**
- Entry Point: `SyncJobServiceAdapter.executePrReviewsJob()` (line 204)
- Services: `PullRequestClientPort.getPullRequestReviews()`, `PullRequestReviewRepository`
- External Dependencies: GitHub API, PullRequestReview DB, SyncJob DB
- Side Effects: Creates/updates review entities
- Files: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java`

**UC-JOB-04: Execute PR_COMMITS Job**
- Entry Point: `SyncJobServiceAdapter.executePrCommitsJob()` (line 272)
- Services: `PullRequestClientPort.getPullRequestCommits()`, `PullRequestCommitRepository`
- External Dependencies: GitHub API, PullRequestCommit DB, SyncJob DB
- Side Effects: Creates/updates commit entities
- Files: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java`

## 3) Domain Candidates

### Should Be Domain

**Data Level Calculation Logic**
- Current Location: `PullRequestServiceAdapter.updateDataLevel()` (line 298)
- File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java`
- Business Rule: Determines if PR has FULL or BASIC data level based on completeness
- Rationale: Core business invariant about data completeness
- Migration: Extract to `PullRequest` domain entity or `DataLevelCalculator` domain service

**Synchronization Need Detection**
- Current Location: `PullRequestServiceAdapter.needsSynchronization()` (line 274)
- File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java`
- Business Rule: Compares `lastSyncedAt` with `updatedAt` to determine if sync needed
- Rationale: Business rule for incremental synchronization
- Migration: Extract to `PullRequest` domain entity method

**Overall Sync Status Determination**
- Current Location: `SyncJobMapper.determineOverallSyncStatus()` (line 85)
- File: `src/main/java/com/optimetric/github/infrastructure/core/mapper/SyncJobMapper.java`
- Business Rule: Aggregates multiple job statuses into overall status (RUNNING > FAILED > COMPLETED > PENDING)
- Rationale: Business logic for status aggregation
- Migration: Extract to `SyncStatus` value object or domain service

**Repository Format Validation**
- Current Location: `PullRequestServiceAdapter.validateInputParameters()` (line 336)
- File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java`
- Business Rule: Validates "owner/repo" format using regex
- Rationale: Domain invariant for repository identity
- Migration: Extract to `RepositoryFullName` value object with validation

**Sync Job Retry Logic**
- Current Location: `SyncJobServiceAdapter.updateJobStatus()` (line 359)
- File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java`
- Business Rule: Retry logic with max attempts (3) and exponential backoff
- Rationale: Business policy for job resilience
- Migration: Extract to `SyncJob` domain entity or `RetryPolicy` value object

### Should Be Application Orchestration

**Pull Request Sync Orchestration**
- Current Location: `SyncJobServiceAdapter.scheduleDetailJobs()` (line 671)
- File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java`
- Responsibility: Orchestrates sequential execution of PR_DETAIL, PR_REVIEWS, PR_COMMITS jobs
- Rationale: Application-level workflow coordination
- Migration: Keep in application service, but extract to use-case class

**Repository Access Validation**
- Current Location: `PullRequestServiceAdapter.validateRepositoryAccess()` (line 253)
- File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java`
- Responsibility: Validates user has access to repository
- Rationale: Application-level authorization orchestration
- Migration: Extract to application service or use-case class

**Job Lifecycle Management**
- Current Location: `SyncJobServiceAdapter.executePrListJob()` (line 47)
- File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java`
- Responsibility: Manages job state transitions, pagination, error handling
- Rationale: Application-level orchestration of external API calls and persistence
- Migration: Extract to use-case classes (e.g., `ExecutePrListJobUseCase`)

**User Repository Initialization Flow**
- Current Location: `RepoServiceAdapter.initUserRepos()` (line 32)
- File: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepoServiceAdapter.java`
- Responsibility: Orchestrates GitHub API call, deduplication, and persistence
- Rationale: Application-level workflow
- Migration: Extract to use-case class

### Should Remain Infrastructure

**HTTP Client Error Handling**
- Current Location: `PullRequestClientAdapter.handleErrorResponse()` (line 143)
- File: `src/main/java/com/optimetric/github/infrastructure/http/PullRequestClientAdapter.java`
- Responsibility: Maps HTTP status codes to domain exceptions
- Rationale: Infrastructure concern for external API communication
- Migration: Keep in infrastructure, but ensure exceptions are domain exceptions

**Entity-to-DTO Mapping**
- Current Location: `PullRequestMapper.toDtos()` (various mapper classes)
- File: `src/main/java/com/optimetric/github/infrastructure/core/mapper/PullRequestMapper.java`
- Responsibility: Converts JPA entities to API DTOs
- Rationale: Infrastructure concern for data transformation
- Migration: Keep in infrastructure, but add domain-to-DTO mapping layer

**JPA Repository Queries**
- Current Location: Various repository interfaces
- Files: `src/main/java/com/optimetric/github/infrastructure/persistence/jpa/*Repository.java`
- Responsibility: Database query definitions
- Rationale: Infrastructure concern
- Migration: Keep in infrastructure, accessed via repository ports

**Rate Limit Header Parsing**
- Current Location: `PullRequestClientAdapter.handleRateLimitError()` (line 223)
- File: `src/main/java/com/optimetric/github/infrastructure/http/PullRequestClientAdapter.java`
- Responsibility: Extracts rate limit info from HTTP headers
- Rationale: Infrastructure concern for external API
- Migration: Keep in infrastructure

## 4) Port & Adapter Candidate List

### Inbound Ports (Use-Cases)

**AuthenticateUserUseCase**
- Location: `com.optimetric.auth.application.usecase`
- Methods:
  - `AuthResponse execute(AuthRequest request)`
- Responsibilities: Exchange GitHub OAuth code for token, fetch user info, create/update user, generate JWT

**GetCurrentUserUseCase**
- Location: `com.optimetric.auth.application.usecase`
- Methods:
  - `UserDto execute(String token)`
- Responsibilities: Validate JWT, retrieve user information

**InitializeUserRepositoriesUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `List<RepositoryDto> execute(String userLogin)`
- Responsibilities: Fetch repos from GitHub API, deduplicate, persist new repos

**QueryRepositoriesUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `List<RepositoryDto> execute(String userLogin, OwnerType ownerType, String ownerLogin)`
- Responsibilities: Query repositories with filtering, validate user access

**GetPullRequestsUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `List<PullRequestDto> execute(String userLogin, String repoFullName)`
- Responsibilities: Validate repository access, retrieve PRs, map to DTOs

**GetPullRequestUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `PullRequestDto execute(String userLogin, String repoFullName, Integer prNumber)`
- Responsibilities: Validate access, retrieve single PR, map to DTO

**GetPullRequestReviewsUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `List<ReviewDto> execute(String userLogin, String repoFullName, Integer prNumber)`
- Responsibilities: Validate access, retrieve reviews, map to DTOs

**GetPullRequestCommitsUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `List<CommitDto> execute(String userLogin, String repoFullName, Integer prNumber)`
- Responsibilities: Validate access, retrieve commits, map to DTOs

**InitiatePullRequestSyncUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `void execute(String userLogin, String repoFullName)`
- Responsibilities: Validate access, check for running jobs, create PR_LIST job, trigger execution

**GetSyncStatusUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `SyncStatusDto execute(String userLogin, String repoFullName)`
- Responsibilities: Validate access, aggregate job statuses, determine overall status

**ExecutePrListJobUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `void execute(String repoFullName)`
- Responsibilities: Fetch PRs from GitHub API page by page, upsert PRs, schedule detail jobs

**ExecutePrDetailJobUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `void execute(String repoFullName, Long pullRequestId)`
- Responsibilities: Fetch PR details, update change statistics, update data level

**ExecutePrReviewsJobUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `void execute(String repoFullName, Long pullRequestId)`
- Responsibilities: Fetch reviews from GitHub API, upsert reviews

**ExecutePrCommitsJobUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `void execute(String repoFullName, Long pullRequestId)`
- Responsibilities: Fetch commits from GitHub API, upsert commits

**InitializeBranchesUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `List<BranchDto> execute(String userLogin, String repoFullName)`
- Responsibilities: Fetch branches from GitHub API, persist to DB

**QueryBranchesUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `List<BranchDto> execute(String userLogin, String repoFullName)`
- Responsibilities: Query branches from DB

**InitializeCommitsUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `List<CommitDto> execute(String userLogin, String repoFullName, String branchName, OffsetDateTime since)`
- Responsibilities: Fetch commits from GitHub API, persist to DB

**QueryCommitsUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `List<CommitDto> execute(String userLogin, String repoFullName, String branchName)`
- Responsibilities: Query commits from DB

**InitializeLanguagesUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `LanguageDto execute(String userLogin, Long repositoryId)`
- Responsibilities: Fetch languages from GitHub API, persist to DB

**QueryLanguagesUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `LanguageDto execute(Long repositoryId)`
- Responsibilities: Query languages from DB

**GetLanguagesForUiUseCase**
- Location: `com.optimetric.github.application.usecase`
- Methods:
  - `LanguageUiDto execute(Long repositoryId)`
- Responsibilities: Query languages, aggregate/format for UI display

### Outbound Ports (Repository Ports)

**UserRepositoryPort**
- Location: `com.optimetric.auth.application.port.out`
- Methods:
  - `Optional<User> findByLogin(String login)`
  - `Optional<User> findByGithubUserId(Long githubUserId)`
  - `User save(User user)`
- Responsibilities: User persistence operations

**RepositoryRepositoryPort**
- Location: `com.optimetric.github.application.port.out`
- Methods:
  - `Optional<Repository> findByFullNameAndUser(String fullName, User user)`
  - `List<Repository> findByUserAndOwnerTypeAndOwnerLogin(User user, OwnerType ownerType, String ownerLogin)`
  - `List<Repository> findByUserAndOwnerType(User user, OwnerType ownerType)`
  - `List<Repository> findByUser(User user)`
  - `List<Repository> findAllByHtmlUrlIn(List<String> urls)`
  - `Repository save(Repository repository)`
  - `List<Repository> saveAll(List<Repository> repositories)`
- Responsibilities: Repository persistence operations

**PullRequestRepositoryPort**
- Location: `com.optimetric.github.application.port.out`
- Methods:
  - `Optional<PullRequest> findById(Long id)`
  - `Optional<PullRequest> findByGithubPrId(Long githubPrId)`
  - `Optional<PullRequest> findByRepositoryAndPrNumber(Repository repository, Integer prNumber)`
  - `List<PullRequest> findByRepositoryOrderByCreatedAtDesc(Repository repository)`
  - `List<PullRequest> findPullRequestsWithoutChangeStats(Repository repository)`
  - `PullRequest save(PullRequest pullRequest)`
- Responsibilities: Pull request persistence operations

**PullRequestReviewRepositoryPort**
- Location: `com.optimetric.github.application.port.out`
- Methods:
  - `Optional<PullRequestReview> findByGithubReviewId(Long githubReviewId)`
  - `List<PullRequestReview> findByPullRequestOrderBySubmittedAtAsc(PullRequest pullRequest)`
  - `PullRequestReview save(PullRequestReview review)`
- Responsibilities: Pull request review persistence operations

**PullRequestCommitRepositoryPort**
- Location: `com.optimetric.github.application.port.out`
- Methods:
  - `Optional<PullRequestCommit> findByGithubRepoIdAndSha(Long githubRepoId, String sha)`
  - `List<PullRequestCommit> findByPullRequestOrderByCommittedAtAsc(PullRequest pullRequest)`
  - `PullRequestCommit save(PullRequestCommit commit)`
- Responsibilities: Pull request commit persistence operations

**SyncJobRepositoryPort**
- Location: `com.optimetric.github.application.port.out`
- Methods:
  - `Optional<SyncJob> findById(Long id)`
  - `Optional<SyncJob> findByRepoFullNameAndJobTypeAndStatus(String repoFullName, SyncJobType jobType, SyncJobStatus status)`
  - `List<SyncJob> findPendingJobsReadyToRun(SyncJobStatus status, OffsetDateTime now)`
  - `List<SyncJob> findByRepoFullNameOrderBySystemCreatedAtDesc(String repoFullName)`
  - `Optional<SyncJob> findFirstByRepoFullNameAndJobTypeAndStatusOrderByCompletedAtDesc(String repoFullName, SyncJobType jobType, SyncJobStatus status)`
  - `List<SyncJob> findStuckJobs(SyncJobStatus status, OffsetDateTime threshold)`
  - `List<SyncJob> findJobsForCleanup(List<SyncJobStatus> statuses, OffsetDateTime threshold)`
  - `SyncJob save(SyncJob job)`
  - `List<SyncJob> saveAll(List<SyncJob> jobs)`
  - `void deleteAll(List<SyncJob> jobs)`
- Responsibilities: Sync job persistence operations

**BranchRepositoryPort**
- Location: `com.optimetric.github.application.port.out`
- Methods:
  - `List<Branch> findByRepository(Repository repository)`
  - `List<Branch> saveAll(List<Branch> branches)`
- Responsibilities: Branch persistence operations

**CommitRepositoryPort**
- Location: `com.optimetric.github.application.port.out`
- Methods:
  - `List<Commit> findByRepositoryAndBranch(Repository repository, String branchName)`
  - `List<Commit> saveAll(List<Commit> commits)`
- Responsibilities: Commit persistence operations

**LanguageRepositoryPort**
- Location: `com.optimetric.github.application.port.out`
- Methods:
  - `Optional<Language> findByRepositoryId(Long repositoryId)`
  - `Language save(Language language)`
- Responsibilities: Language persistence operations

### Outbound Ports (External Service Ports)

**GitHubApiPort**
- Location: `com.optimetric.github.application.port.out`
- Methods:
  - `AccessTokenResponse getAccessTokenFromCode(String code)`
  - `UserInfo getUserInfo(String accessToken)`
  - `List<PullRequestInfo> getPullRequests(String accessToken, String owner, String repo, int page)`
  - `PullRequestDetailInfo getPullRequestDetails(String accessToken, String owner, String repo, int prNumber)`
  - `List<ReviewInfo> getPullRequestReviews(String accessToken, String owner, String repo, int prNumber)`
  - `List<CommitInfo> getPullRequestCommits(String accessToken, String owner, String repo, int prNumber)`
  - `List<RepositoryInfo> getUserRepos(String accessToken)`
  - `List<BranchInfo> getBranches(String accessToken, String owner, String repo)`
  - `List<CommitInfo> getCommits(String accessToken, String owner, String repo, String branchName, OffsetDateTime since)`
  - `LanguageInfo getLanguages(String accessToken, String owner, String repo)`
- Responsibilities: All GitHub API interactions (replaces current client ports)

**JwtServicePort** (Keep as-is)
- Location: `com.optimetric.auth.application.port.out`
- Methods:
  - `String createToken(String login)`
  - `String getLoginFromToken(String token)`
- Responsibilities: JWT token generation and validation

**ClockPort**
- Location: `com.optimetric.shared.application.port.out`
- Methods:
  - `OffsetDateTime now()`
- Responsibilities: Time abstraction for testability

**JobSchedulerPort** (Future)
- Location: `com.optimetric.github.application.port.out`
- Methods:
  - `void scheduleJob(SyncJob job, OffsetDateTime executeAt)`
  - `void cancelJob(Long jobId)`
- Responsibilities: Job scheduling abstraction

## 5) Data Contracts Audit

### API DTOs Used Outside API Layer

**PullRequestDto** used in:
- `src/main/java/com/optimetric/github/application/port/PullRequestServicePort.java` (line 3)
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java` (line 5)
- `src/main/java/com/optimetric/github/infrastructure/core/mapper/PullRequestMapper.java` (line 4)

**ReviewDto** used in:
- `src/main/java/com/optimetric/github/application/port/PullRequestServicePort.java` (line 4)
- `src/main/java/com/optimetric/github/infrastructure/core/mapper/PullRequestMapper.java` (line 5)

**ReviewCommentDto** used in:
- `src/main/java/com/optimetric/github/application/port/PullRequestServicePort.java` (line 5)
- `src/main/java/com/optimetric/github/infrastructure/core/mapper/PullRequestMapper.java` (line 6)

**SyncStatusDto** used in:
- `src/main/java/com/optimetric/github/application/port/PullRequestServicePort.java` (line 6)
- `src/main/java/com/optimetric/github/infrastructure/core/mapper/SyncJobMapper.java` (line 3)

**RepoDto** used in:
- `src/main/java/com/optimetric/github/application/port/RepoServicePort.java` (line 5)
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepoServiceAdapter.java` (line 12)
- `src/main/java/com/optimetric/github/infrastructure/core/mapper/RepoMapper.java` (line 6)

**RepoInitrequest** used in:
- `src/main/java/com/optimetric/github/application/port/RepoServicePort.java` (line 4)
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepoServiceAdapter.java` (line 11)

**LanguageDto** used in:
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/LanguageServiceAdapter.java` (line 4)
- `src/main/java/com/optimetric/github/infrastructure/http/LanguageClientAdapter.java` (line 3)
- `src/main/java/com/optimetric/github/infrastructure/core/mapper/LanguageMapper.java` (line 3)

**LanguageUiDto** used in:
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/LanguageServiceAdapter.java` (line 5)
- `src/main/java/com/optimetric/github/infrastructure/core/mapper/LanguageMapper.java` (line 4)

**BranchDto** used in:
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/BranchServiceAdapter.java` (line 10)
- `src/main/java/com/optimetric/github/infrastructure/http/BranchClientAdapter.java` (line 13)
- `src/main/java/com/optimetric/github/infrastructure/core/mapper/BranchMapper.java` (line 3)

**CommitDto** used in:
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/CommitServiceAdapter.java` (line 4)
- `src/main/java/com/optimetric/github/infrastructure/http/CommitClientAdapter.java` (line 11)
- `src/main/java/com/optimetric/github/infrastructure/core/mapper/CommitMapper.java` (line 3)

**AuthRequest, AuthResponse, GitHubUserDto** used in:
- `src/main/java/com/optimetric/auth/application/AuthServicePort.java` (lines 3-4, 6)
- `src/main/java/com/optimetric/auth/infrastructure/persistence/adapter/AuthServiceAdapter.java` (lines 7-10)

### JPA Entities Used Outside Infrastructure

**RepositoryEntity** used in:
- `src/main/java/com/optimetric/github/application/port/RepoServicePort.java` (line 6)
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java` (line 52, 79, 184, 260)
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java` (line 68, 169, 231, 299, 554)

**PullRequestEntity** used in:
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java` (line 56, 83, 115, 152, 274, 298, 326)
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java` (line 163, 168, 225, 230, 293, 298, 572, 605, 621, 646, 680)

**SyncJobEntity** used in:
- `src/main/java/com/optimetric/github/application/port/SyncJobServicePort.java` (lines 3, 53, 69, 77, 86)
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java` (line 188, 199, 230, 233-236)
- `src/main/java/com/optimetric/github/infrastructure/core/mapper/SyncJobMapper.java` (line 4)

**User** (from auth) used in:
- `src/main/java/com/optimetric/auth/application/AuthServicePort.java` (line 5)
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepoServiceAdapter.java` (line 9, 34, 49, 66, 73, 97)
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java` (line 257)

**DataLevel** enum used in:
- `src/main/java/com/optimetric/github/api/response/PullRequestDto.java` (line 3)
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java` (line 310)

**SyncJobStatus, SyncJobType** used in:
- `src/main/java/com/optimetric/github/application/port/SyncJobServicePort.java` (lines 4-5)
- `src/main/java/com/optimetric/github/infrastructure/core/mapper/SyncJobMapper.java` (line 5)

**OwnerType** used in:
- `src/main/java/com/optimetric/github/application/port/RepoServicePort.java` (line 3)

### Spring/JPA/WebClient Coupling in Pure Logic

**@Transactional annotations** in service adapters:
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java` (lines 19, 42, 68, 100, 137, 174, 216, 297, 325)
- `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java` (lines 16, 46, 141, 203, 271, 339, 358, 401, 410, 418, 441, 464, 496)

**@Service annotations** mixing infrastructure with business:
- All service adapters use `@Service` annotation
- Business logic methods contain Spring transaction management

**WebClient usage** in client adapters:
- `src/main/java/com/optimetric/github/infrastructure/http/PullRequestClientAdapter.java` (line 30)
- WebClient is Spring WebFlux specific, tightly couples to reactive framework

**JPA Repository injection** in service adapters:
- All service adapters directly inject JPA repositories
- No abstraction layer between application and persistence

## 6) Refactor Roadmap

### Slice 1: Extract Domain Models and Value Objects

**Goals:**
- Create domain entities (PullRequest, Repository, SyncJob, User) without JPA annotations
- Extract value objects (RepositoryFullName, DataLevel enum)
- Move business rules from adapters to domain entities
- Create domain exceptions

**Files Impacted:**
- Create: `src/main/java/com/optimetric/github/domain/entity/PullRequest.java`
- Create: `src/main/java/com/optimetric/github/domain/entity/Repository.java`
- Create: `src/main/java/com/optimetric/github/domain/entity/SyncJob.java`
- Create: `src/main/java/com/optimetric/github/domain/valueobject/RepositoryFullName.java`
- Create: `src/main/java/com/optimetric/github/domain/valueobject/DataLevel.java` (move from infrastructure)
- Create: `src/main/java/com/optimetric/github/domain/service/DataLevelCalculator.java`
- Create: `src/main/java/com/optimetric/auth/domain/entity/User.java`
- Modify: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java` (extract business logic)
- Modify: `src/main/java/com/optimetric/github/infrastructure/core/mapper/SyncJobMapper.java` (extract status aggregation)

**Success Criteria:**
- Domain entities contain business logic (needsSynchronization, updateDataLevel)
- No JPA annotations in domain layer
- Domain layer has no dependencies on infrastructure or API layers
- All business rules are testable without Spring/JPA

**Risks:**
- Risk: Large number of files to create
- Mitigation: Start with one entity (PullRequest), establish pattern, then replicate
- Risk: Breaking existing functionality during extraction
- Mitigation: Keep existing adapters working, add domain layer in parallel

### Slice 2: Create Application DTOs and Separate from API DTOs

**Goals:**
- Create application-layer DTOs separate from API DTOs
- Update ports to use application DTOs instead of API DTOs
- Create mapping layer between application and API DTOs

**Files Impacted:**
- Create: `src/main/java/com/optimetric/github/application/dto/PullRequestDto.java`
- Create: `src/main/java/com/optimetric/github/application/dto/RepositoryDto.java`
- Create: `src/main/java/com/optimetric/github/application/dto/SyncStatusDto.java`
- Create: `src/main/java/com/optimetric/github/api/mapper/ApplicationToApiMapper.java`
- Modify: `src/main/java/com/optimetric/github/application/port/PullRequestServicePort.java` (use application DTOs)
- Modify: `src/main/java/com/optimetric/github/application/port/RepoServicePort.java` (use application DTOs)
- Modify: `src/main/java/com/optimetric/github/api/PullRequestController.java` (map application DTOs to API DTOs)

**Success Criteria:**
- Application ports no longer import API DTOs
- API layer maps application DTOs to API DTOs
- No circular dependencies between layers

**Risks:**
- Risk: Duplicate DTO classes (application vs API)
- Mitigation: Document clearly which DTOs belong to which layer
- Risk: Mapping overhead
- Mitigation: Use mapper classes, consider MapStruct for performance

### Slice 3: Extract Repository Ports and Implement Adapters

**Goals:**
- Create repository port interfaces in application layer
- Implement repository adapters in infrastructure
- Replace direct JPA repository usage with port calls

**Files Impacted:**
- Create: `src/main/java/com/optimetric/github/application/port/out/PullRequestRepositoryPort.java`
- Create: `src/main/java/com/optimetric/github/application/port/out/RepositoryRepositoryPort.java`
- Create: `src/main/java/com/optimetric/github/application/port/out/SyncJobRepositoryPort.java`
- Create: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestRepositoryAdapter.java`
- Create: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepositoryRepositoryAdapter.java`
- Create: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobRepositoryAdapter.java`
- Modify: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java` (use repository ports)
- Modify: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java` (use repository ports)

**Success Criteria:**
- Service adapters no longer directly inject JPA repositories
- All persistence operations go through repository ports
- Repository adapters handle entity-to-domain mapping

**Risks:**
- Risk: Performance overhead from additional abstraction layer
- Mitigation: Keep adapters thin, delegate directly to JPA repositories
- Risk: Complex mapping between domain and entities
- Mitigation: Use mapper classes, consider bidirectional mapping

### Slice 4: Extract Use-Case Classes from Service Adapters

**Goals:**
- Extract use-case classes from service adapter methods
- Use-cases orchestrate domain logic and call repository/external ports
- Service adapters become thin wrappers or are removed

**Files Impacted:**
- Create: `src/main/java/com/optimetric/github/application/usecase/GetPullRequestsUseCase.java`
- Create: `src/main/java/com/optimetric/github/application/usecase/InitiatePullRequestSyncUseCase.java`
- Create: `src/main/java/com/optimetric/github/application/usecase/ExecutePrListJobUseCase.java`
- Create: `src/main/java/com/optimetric/github/application/usecase/ExecutePrDetailJobUseCase.java`
- Modify: `src/main/java/com/optimetric/github/api/PullRequestController.java` (call use-cases)
- Modify: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java` (delegate to use-cases or remove)

**Success Criteria:**
- Controllers call use-cases directly
- Use-cases contain orchestration logic
- Service adapters are removed or become simple facades
- Business logic is in domain or use-cases, not adapters

**Risks:**
- Risk: Large refactoring affecting many files
- Mitigation: Refactor one use-case at a time, keep tests passing
- Risk: Breaking existing API contracts
- Mitigation: Keep controller signatures unchanged, only change internal implementation

### Slice 5: Extract External Service Ports (GitHub API)

**Goals:**
- Create GitHubApiPort in application layer
- Implement adapter in infrastructure
- Replace client port interfaces with single GitHubApiPort
- Create domain models for GitHub API responses

**Files Impacted:**
- Create: `src/main/java/com/optimetric/github/application/port/out/GitHubApiPort.java`
- Create: `src/main/java/com/optimetric/github/domain/model/GitHubPullRequestInfo.java`
- Create: `src/main/java/com/optimetric/github/infrastructure/http/GitHubApiAdapter.java`
- Modify: `src/main/java/com/optimetric/github/infrastructure/http/PullRequestClientAdapter.java` (refactor to use domain models)
- Modify: All use-cases that call GitHub API (use GitHubApiPort)

**Success Criteria:**
- Application layer has single port for all GitHub API operations
- Infrastructure adapters map GitHub HTTP DTOs to domain models
- No infrastructure HTTP DTOs leak into application layer

**Risks:**
- Risk: Large interface with many methods
- Mitigation: Consider splitting by domain (PullRequestApi, RepositoryApi) if too large
- Risk: Breaking existing client adapters
- Mitigation: Keep old adapters working, create new adapter in parallel, switch gradually

### Slice 6: Extract Clock and Remove Spring Annotations from Domain/Application

**Goals:**
- Create ClockPort for time abstraction
- Remove @Transactional from domain/application code
- Move transaction boundaries to infrastructure adapters
- Remove @Service annotations, use @Component or manual wiring

**Files Impacted:**
- Create: `src/main/java/com/optimetric/shared/application/port/out/ClockPort.java`
- Create: `src/main/java/com/optimetric/shared/infrastructure/adapter/SystemClockAdapter.java`
- Modify: All use-cases and domain services (use ClockPort instead of OffsetDateTime.now())
- Modify: All service adapters (move @Transactional to adapter methods only)
- Modify: Configuration classes (manual bean wiring if needed)

**Success Criteria:**
- Domain and application layers have no Spring annotations
- Time-dependent code uses ClockPort
- Transaction management is infrastructure concern only
- Domain/application code is framework-agnostic

**Risks:**
- Risk: Forgetting to inject ClockPort in many places
- Mitigation: Use dependency injection, create ClockPort bean in configuration
- Risk: Performance impact of abstraction
- Mitigation: ClockPort implementation is trivial, no measurable impact


