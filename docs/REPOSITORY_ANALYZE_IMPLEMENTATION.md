# Repository Analyze Endpoint + Sync Orchestrator (MVP) - Implementation Summary

## Overview

This feature implements an analyze endpoint that automatically detects missing data components and enqueues sync jobs. This reduces frontend dependency on manual init endpoints and enables backend-owned sync orchestration.

## Implementation Details

### Architecture

- **Pattern**: Hexagonal Architecture
- **Layer**: Application + Infrastructure
- **No GitHub API calls**: Database-only analysis and job enqueueing
- **Idempotent**: Prevents duplicate job creation

### Components Created

#### 1. Enums
- `AnalyzeState`: SYNC_STARTED, ALREADY_READY, SYNC_ALREADY_RUNNING
- `EnqueuedJobType`: BRANCH_SYNC, PR_LIST, LANGUAGE_SYNC, COMMIT_SYNC

#### 2. DTOs
- `RepositoryAnalyzeRequest`: Request DTO with fullName
- `RepositoryAnalyzeResponse`: Response DTO with state, enqueued jobs, and status endpoint

#### 3. Service
- `RepositoryAnalyzeServicePort`: Application port interface
- `RepositoryAnalyzeServiceAdapter`: Infrastructure adapter implementation

#### 4. Controller
- `RepositoryAnalyzeController`: REST endpoint

#### 5. Updated
- `SyncJobType`: Added BRANCH_SYNC, COMMIT_SYNC, LANGUAGE_SYNC

## Endpoint

### POST `/api/github/repositories/analyze`

**Request:**
```json
{
  "fullName": "owner/repo"
}
```

**Response:**
```json
{
  "state": "SYNC_STARTED|ALREADY_READY|SYNC_ALREADY_RUNNING",
  "enqueued": ["BRANCH_SYNC", "PR_LIST", "LANGUAGE_SYNC", "COMMIT_SYNC"],
  "statusEndpoint": "/api/github/repositories/sync/status"
}
```

## Readiness -> Job Mapping

| Missing Component | Job Type | Notes |
|------------------|----------|-------|
| BRANCHES | BRANCH_SYNC | Always enqueued if missing |
| PRS | PR_LIST | Triggers PR_DETAIL -> PR_REVIEWS chain |
| LANGUAGES | LANGUAGE_SYNC | Always enqueued if missing |
| COMMITS | COMMIT_SYNC | Only if branches are ready (dependency) |
| PR_DETAILS | - | Handled by PR job chain (PR_LIST -> PR_DETAIL) |
| REVIEWS | - | Handled by PR job chain (PR_DETAIL -> PR_REVIEWS) |

## Job Enqueueing Rules

### Dependency Handling
- **COMMIT_SYNC**: Only enqueued if `branchesReady` is true
  - Reason: Commits require branches to exist first
  - If branches are missing, only BRANCH_SYNC is enqueued

### PR Job Chain
- **PR_LIST** triggers the chain: PR_LIST -> PR_DETAIL -> PR_REVIEWS
- If PRS exist but PR_DETAILS/REVIEWS are missing, the chain will handle it automatically
- No separate enqueueing needed for PR_DETAILS/REVIEWS in MVP

### Idempotency
- Before enqueueing, checks if a PENDING or RUNNING job of the same type exists
- If exists, skips enqueueing (prevents duplicate jobs)
- Uses `SyncJobRepository.findByRepoFullNameAndJobTypeAndStatus()`

## State Logic

1. **ALREADY_READY**: All data components are ready (readiness.state == READY)
   - Returns immediately, no jobs enqueued

2. **SYNC_ALREADY_RUNNING**: Active jobs (PENDING or RUNNING) exist for the repository
   - Prevents duplicate job creation
   - Returns immediately

3. **SYNC_STARTED**: Jobs were enqueued based on missing components
   - Returns list of enqueued job types

## Error Handling

- **404 Not Found**: If repository with given `fullName` doesn't exist
  - Exception: `RepositoryNotFoundException`
  - Message: "Repository not found with fullName: {fullName}. Please initialize the repository first."

## Testing

### Example cURL Request

```bash
curl -X POST http://localhost:8080/api/github/repositories/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "facebook/react"
  }'
```

### Example Response (SYNC_STARTED)

```json
{
  "state": "SYNC_STARTED",
  "enqueued": ["BRANCH_SYNC", "PR_LIST", "LANGUAGE_SYNC"],
  "statusEndpoint": "/api/github/repositories/sync/status"
}
```

### Example Response (ALREADY_READY)

```json
{
  "state": "ALREADY_READY",
  "enqueued": [],
  "statusEndpoint": "/api/github/repositories/sync/status"
}
```

### Example Response (SYNC_ALREADY_RUNNING)

```json
{
  "state": "SYNC_ALREADY_RUNNING",
  "enqueued": [],
  "statusEndpoint": "/api/github/repositories/sync/status"
}
```

## Files Created/Modified

### Created Files

1. `src/main/java/com/optimetric/github/domain/enumeration/AnalyzeState.java`
2. `src/main/java/com/optimetric/github/domain/enumeration/EnqueuedJobType.java`
3. `src/main/java/com/optimetric/github/api/request/RepositoryAnalyzeRequest.java`
4. `src/main/java/com/optimetric/github/api/response/RepositoryAnalyzeResponse.java`
5. `src/main/java/com/optimetric/github/application/port/RepositoryAnalyzeServicePort.java`
6. `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepositoryAnalyzeServiceAdapter.java`
7. `src/main/java/com/optimetric/github/api/RepositoryAnalyzeController.java`

### Modified Files

1. `src/main/java/com/optimetric/github/infrastructure/persistence/entity/SyncJobType.java`
   - Added: BRANCH_SYNC, COMMIT_SYNC, LANGUAGE_SYNC

## Integration with Existing Services

### Uses
- `RepoSyncStatusServicePort`: Gets repository readiness status
- `SyncJobServicePort`: Creates and manages sync jobs
- `SyncJobRepository`: Checks for existing jobs (idempotency)

### Job Execution
- Jobs are created with status PENDING
- Job execution is handled by existing job runner/scheduler (not part of this MVP)
- COMMIT_SYNC job payload (since parameter) is handled by job runner (default: 90 days)

## Future Enhancements

This MVP implementation provides the foundation for:

1. **Automatic Retry**: Failed jobs can be automatically retried
2. **Job Priority**: Different job types can have different priorities
3. **Progress Tracking**: Real-time progress updates via status endpoint
4. **Configurable Periods**: COMMIT_SYNC since parameter can be configurable
5. **Job Scheduling**: More sophisticated scheduling based on repository size/activity

## Notes

- **No GitHub API calls**: This endpoint is purely database-driven
- **Idempotent**: Safe to call multiple times (won't create duplicate jobs)
- **Dependency-aware**: Respects dependencies (e.g., branches before commits)
- **Chain-aware**: PR job chain is handled automatically
- **Status endpoint**: Returns status endpoint URL for progress tracking

## Workflow Example

1. Frontend calls `/api/github/repositories/analyze` with repo full name
2. Backend checks readiness using `RepoSyncStatusService`
3. If not ready, enqueues missing component jobs
4. Frontend polls `/api/github/repositories/sync/status` to track progress
5. When ready, frontend can call metrics endpoints

This reduces frontend complexity and enables backend-owned sync orchestration.
