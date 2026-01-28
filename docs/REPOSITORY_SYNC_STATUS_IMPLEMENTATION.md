# Repository Sync Status + Data Readiness (MVP) - Implementation Summary

## Overview

This feature implements a repository sync status endpoint that checks data readiness for metrics calculation. It provides information about which data components are available and which are missing, helping to distinguish between "0 metrics" (no data) and "0 metrics" (data exists but value is 0).

## Implementation Details

### Architecture

- **Pattern**: Hexagonal Architecture
- **Layer**: Application + Infrastructure
- **No GitHub API calls**: Database-only queries
- **Performance**: Uses EXISTS queries with LIMIT 1 for optimal performance

### Components Created

#### 1. Enums
- `ReadinessState`: NOT_READY, PARTIAL, READY
- `MissingComponent`: BRANCHES, PRS, PR_DETAILS, REVIEWS, COMMITS, LANGUAGES

#### 2. DTOs
- `RepositorySyncStatusRequest`: Request DTO with fullName
- `RepositoryReadinessDto`: Response DTO with state, missing, flags
- `RepositoryReadinessFlags`: Nested DTO for individual flags

#### 3. Repository Methods
Added EXISTS query methods to all relevant repositories:
- `BranchRepository.existsByRepositoryId()`
- `CommitRepository.existsByRepositoryId()`
- `PullRequestRepository.existsByRepositoryId()`
- `PullRequestRepository.existsWithDetailsByRepositoryId()` (for PR_DETAILS)
- `PullRequestReviewRepository.existsByRepositoryId()` (with JOIN)
- `LanguageRepository.existsByRepositoryId()`

#### 4. Service
- `RepoSyncStatusServicePort`: Application port interface
- `RepoSyncStatusServiceAdapter`: Infrastructure adapter implementation

#### 5. Controller
- `RepositorySyncStatusController`: REST endpoint

## Endpoint

### POST `/api/github/repositories/sync/status`

**Request:**
```json
{
  "fullName": "owner/repo"
}
```

**Response:**
```json
{
  "state": "READY|PARTIAL|NOT_READY",
  "missing": ["BRANCHES", "PRS", "PR_DETAILS", "REVIEWS", "COMMITS", "LANGUAGES"],
  "flags": {
    "branchesReady": true,
    "commitsReady": true,
    "pullRequestsReady": true,
    "prDetailsReady": true,
    "reviewsReady": true,
    "languagesReady": true
  }
}
```

## Readiness Rules (MVP)

1. **branchesReady**: `github_branches` table has at least one record for repository_id
2. **commitsReady**: `github_repository_commits` table has at least one record for repository_id
3. **pullRequestsReady**: `github_pull_requests` table has at least one record for repository_id
4. **prDetailsReady**: `github_pull_requests` table has at least one record with `additions IS NOT NULL` for repository_id
5. **reviewsReady**: `github_pull_request_reviews` table has at least one record (JOIN with pull_requests via pull_request_id)
6. **languagesReady**: `github_repository_languages` table has at least one record for repository_id

## State Calculation

- **READY**: All flags are `true` (missing list is empty)
- **NOT_READY**: All flags are `false` (missing list has 6 items)
- **PARTIAL**: Some flags are `true`, some are `false` (missing list has 1-5 items)

## Missing Components Order

The `missing` array is always built in this fixed order:
1. BRANCHES
2. PRS
3. PR_DETAILS
4. REVIEWS
5. COMMITS
6. LANGUAGES

This ensures consistent responses regardless of which components are missing.

## Error Handling

- **404 Not Found**: If repository with given `fullName` doesn't exist
  - Exception: `RepositoryNotFoundException`
  - Message: "Repository not found with fullName: {fullName}. Please initialize the repository first."

## Testing

### Example cURL Request

```bash
curl -X POST http://localhost:8080/api/github/repositories/sync/status \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "facebook/react"
  }'
```

### Example Response (READY)

```json
{
  "state": "READY",
  "missing": [],
  "flags": {
    "branchesReady": true,
    "commitsReady": true,
    "pullRequestsReady": true,
    "prDetailsReady": true,
    "reviewsReady": true,
    "languagesReady": true
  }
}
```

### Example Response (PARTIAL)

```json
{
  "state": "PARTIAL",
  "missing": ["PR_DETAILS", "REVIEWS"],
  "flags": {
    "branchesReady": true,
    "commitsReady": true,
    "pullRequestsReady": true,
    "prDetailsReady": false,
    "reviewsReady": false,
    "languagesReady": true
  }
}
```

### Example Response (NOT_READY)

```json
{
  "state": "NOT_READY",
  "missing": ["BRANCHES", "PRS", "PR_DETAILS", "REVIEWS", "COMMITS", "LANGUAGES"],
  "flags": {
    "branchesReady": false,
    "commitsReady": false,
    "pullRequestsReady": false,
    "prDetailsReady": false,
    "reviewsReady": false,
    "languagesReady": false
  }
}
```

### Example Response (404 - Repository Not Found)

```json
{
  "timestamp": "2026-01-28T18:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Repository not found with fullName: owner/repo. Please initialize the repository first.",
  "path": "/api/github/repositories/sync/status"
}
```

## Files Created/Modified

### Created Files

1. `src/main/java/com/optimetric/github/domain/enumeration/ReadinessState.java`
2. `src/main/java/com/optimetric/github/domain/enumeration/MissingComponent.java`
3. `src/main/java/com/optimetric/github/api/request/RepositorySyncStatusRequest.java`
4. `src/main/java/com/optimetric/github/api/response/RepositoryReadinessFlags.java`
5. `src/main/java/com/optimetric/github/api/response/RepositoryReadinessDto.java`
6. `src/main/java/com/optimetric/github/application/port/RepoSyncStatusServicePort.java`
7. `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepoSyncStatusServiceAdapter.java`
8. `src/main/java/com/optimetric/github/api/RepositorySyncStatusController.java`

### Modified Files

1. `src/main/java/com/optimetric/github/infrastructure/persistence/jpa/BranchRepository.java` - Added `existsByRepositoryId()`
2. `src/main/java/com/optimetric/github/infrastructure/persistence/jpa/CommitRepository.java` - Added `existsByRepositoryId()`
3. `src/main/java/com/optimetric/github/infrastructure/persistence/jpa/PullRequestRepository.java` - Added `existsByRepositoryId()` and `existsWithDetailsByRepositoryId()`
4. `src/main/java/com/optimetric/github/infrastructure/persistence/jpa/PullRequestReviewRepository.java` - Added `existsByRepositoryId()` with JOIN
5. `src/main/java/com/optimetric/github/infrastructure/persistence/jpa/LanguageRepository.java` - Added `existsByRepositoryId()`

## Future Enhancements

This MVP implementation provides the foundation for:

1. **Metrics Endpoint Gating**: Return `202 Accepted` when data is not ready, `200 OK` when ready
2. **Backend-Owned Sync Orchestration**: Automatically trigger sync jobs based on missing components
3. **Progress Tracking**: Show sync progress percentage
4. **Component-Level Details**: Provide more granular information about each component's status

## Notes

- **No GitHub API calls**: This endpoint is purely database-driven for performance
- **Deterministic**: Always returns consistent results based on current DB state
- **Fast**: Uses EXISTS queries optimized for quick checks
- **Review JOIN**: Reviews check uses JOIN since `github_pull_request_reviews` doesn't have `repository_id` column
