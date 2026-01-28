# Commit Mesajları - Language Unique Constraint

## Commit 1: Database Migration - Deduplicate
```
db: deduplicate github_repository_languages

Remove duplicate entries for (repository_id, language_name) pairs.
Keeps the most recent record based on last_synced_at timestamp.

- Add V002 migration to clean existing duplicates
- Uses ROW_NUMBER() to identify and delete duplicate records
- Keeps record with most recent last_synced_at or highest id
```

## Commit 2: Database Migration - Unique Constraint
```
db: add unique constraint (repository_id, language_name)

Add unique constraint to prevent duplicate language entries per repository.

- Add V003 migration with UNIQUE constraint
- Constraint name: uk_repo_language
- Prevents future duplicates at database level
```

## Commit 3: Feature - Idempotent Language Init
```
feat: make languages init idempotent (upsert)

Refactor initRepoLanguages to use upsert pattern instead of delete-then-insert.
This makes the endpoint idempotent and prevents race conditions.

Changes:
- Add findByRepository_IdAndLanguageName to LanguageRepository
- Update LanguageServiceAdapter.initRepoLanguages() to:
  * Update existing languages instead of deleting all
  * Create new languages only if they don't exist
  * Delete languages that are no longer in GitHub response
- Method is now safe to call multiple times for same repository

Benefits:
- No duplicate entries even with concurrent calls
- Preserves existing data during updates
- Idempotent behavior (AC2 requirement)
```

## Commit 4: Fix - Build Issues
```
fix: resolve compilation errors and Lombok configuration

Fix build issues encountered after branch switch.

- Fix Lombok annotation processor version in pom.xml
- Rename RepoInitrequest.java to RepoInitRequest.java (file name mismatch)
- Fix PullRequestCommitEntity class name (giPullRequestCommitEntity -> PullRequestCommitEntity)
```

## Commit 5: Test - Add Test Scripts and Documentation
```
test: add language unique constraint test scripts and guide

Add comprehensive testing documentation and automation scripts.

- Add TEST_GUIDE.md with step-by-step testing instructions
- Add check_duplicate_languages.sql for duplicate detection
- Add test_language_idempotency.sh for automated API testing
- Add verify_no_duplicates.sql for database verification queries

These scripts help verify:
- AC1: DB uniqueness constraint works
- AC2: Idempotent init behavior
- AC3: No regression in query endpoints
- AC4: Data correctness
```

---

## Tek Commit İçin (Eğer hepsini birlikte commit edeceksen):

```
feat: add unique constraint and idempotent upsert for repository languages

Implement unique constraint on (repository_id, language_name) and refactor
language initialization to use upsert pattern for idempotency.

Database Changes:
- V002: Deduplicate existing github_repository_languages records
- V003: Add UNIQUE constraint (repository_id, language_name)

Code Changes:
- Add findByRepository_IdAndLanguageName to LanguageRepository
- Refactor LanguageServiceAdapter.initRepoLanguages() to upsert pattern:
  * Update existing languages instead of delete-all
  * Create only new languages
  * Remove languages no longer in GitHub response

Fixes:
- Fix Lombok annotation processor version in pom.xml
- Fix file name: RepoInitrequest.java -> RepoInitRequest.java
- Fix class name: giPullRequestCommitEntity -> PullRequestCommitEntity

Testing:
- Add TEST_GUIDE.md with comprehensive testing instructions
- Add test scripts for duplicate detection and API testing
- Add SQL queries for database verification

This ensures:
✅ AC1: DB uniqueness constraint prevents duplicates
✅ AC2: Idempotent init (safe to call multiple times)
✅ AC3: No regression in query endpoints
✅ AC4: Data correctness maintained

Closes: Language unique constraint task
```
