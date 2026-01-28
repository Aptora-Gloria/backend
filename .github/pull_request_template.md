## Description

<!-- Describe your changes -->

## Type of Change

- [ ] Bug fix
- [ ] New feature
- [ ] Refactoring
- [ ] Documentation update

## Repo Slice Architecture Compliance Checklist

**⚠️ IMPORTANT: All items must be checked before merging**

### Application Layer Rules

- [ ] Application layer does not import API layer (`com.optimetric.github.api`)
  - Verification: `git grep "com\.optimetric\.github\.api" src/main/java/com/optimetric/github/application`
  - Expected: No matches

- [ ] Application layer does not import Infrastructure layer (`com.optimetric.github.infrastructure`)
  - Verification: `git grep "com\.optimetric\.github\.infrastructure" src/main/java/com/optimetric/github/application`
  - Expected: No matches

- [ ] Application layer does not import Persistence entities (`infrastructure.persistence.entity`)
  - Verification: `git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/application`
  - Expected: No matches

- [ ] Application layer does not import JPA Repositories (`RepositoryRepository`)
  - Verification: `git grep "RepositoryRepository" src/main/java/com/optimetric/github/application`
  - Expected: No matches

- [ ] Application layer does not use `RepositoryEntity`
  - Verification: `git grep "RepositoryEntity" src/main/java/com/optimetric/github/application`
  - Expected: No matches

### API Layer Rules

- [ ] API layer does not import Persistence entities
  - Verification: `git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/api`
  - Expected: No matches

- [ ] Controller calls Use Cases, not Adapters directly
  - Verification: `git grep "UseCase\|ServiceAdapter" src/main/java/com/optimetric/github/api/RepoController.java`
  - Expected: UseCase imports present, ServiceAdapter imports absent

### Infrastructure Layer Rules

- [ ] Infrastructure adapters only implement ports, no business logic orchestration
  - Verification: Check adapter files for business logic (should only map/transform data)

### Port/Adapter Pattern

- [ ] Outbound ports are in `application/port/out`
  - Verification: `find src/main/java/com/optimetric/github/application/port/out -name "*.java"`

- [ ] Adapters are in `infrastructure/*/adapter`
  - Verification: `find src/main/java/com/optimetric/github/infrastructure -name "*Adapter.java"`

### Use Case Pattern

- [ ] Use Cases are in `application/usecase`
  - Verification: `find src/main/java/com/optimetric/github/application/usecase -name "*UseCase.java"`

### Quick Verification

Run the verification script for Repo Slice only:

```bash
./scripts/verify-repo-slice-only.sh
```

Or run full application layer verification (includes other slices):

```bash
./scripts/verify-repo-slice-architecture.sh
```

Or run manual checks:

```bash
# Quick checks
git grep "com\.optimetric\.github\.api" src/main/java/com/optimetric/github/application
git grep "com\.optimetric\.github\.infrastructure" src/main/java/com/optimetric/github/application
git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/application
git grep "RepositoryRepository" src/main/java/com/optimetric/github/application
```

## Testing

<!-- Describe how you tested your changes -->

## Related Issues

<!-- Link related issues here -->

## Additional Notes

<!-- Any additional information -->
