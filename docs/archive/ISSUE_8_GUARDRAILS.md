# ISSUE 8 — Guardrails (Repo Slice Sonrası Tekrar Bozulmasın)

## Amaç

Repo slice refactor'ü tamamlandıktan sonra, mimari kuralların tekrar bozulmaması için guardrails (koruma kuralları) oluşturmak.

## Mimari Kurallar Checklist

PR review sırasında kullanılacak checklist:

### ✅ Application Layer Kuralları

- [ ] **Application → API Import Yasak**
  - `src/main/java/com/optimetric/github/application` içinde `com.optimetric.github.api.*` importu yok
  - Doğrulama: `git grep "com\.optimetric\.github\.api" src/main/java/com/optimetric/github/application`

- [ ] **Application → Infrastructure Import Yasak**
  - `src/main/java/com/optimetric/github/application` içinde `com.optimetric.github.infrastructure.*` importu yok
  - Doğrulama: `git grep "com\.optimetric\.github\.infrastructure" src/main/java/com/optimetric/github/application`

- [ ] **Application → Persistence Entity Import Yasak**
  - `src/main/java/com/optimetric/github/application` içinde `RepositoryEntity`, `User` (auth entity) gibi persistence entity importu yok
  - Doğrulama: `git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/application`

- [ ] **Application → JPA Repository Import Yasak**
  - `src/main/java/com/optimetric/github/application` içinde `RepositoryRepository` gibi JPA repository importu yok
  - Doğrulama: `git grep "RepositoryRepository\|.*Repository" src/main/java/com/optimetric/github/application`

### ✅ API Layer Kuralları

- [ ] **API → Persistence Entity Import Yasak**
  - `src/main/java/com/optimetric/github/api` içinde `RepositoryEntity`, `User` gibi persistence entity importu yok
  - Doğrulama: `git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/api`

- [ ] **API → Application DTO Kullanımı**
  - Controller'lar application DTO'ları kullanıyor, infrastructure entity'leri kullanmıyor
  - Doğrulama: `git grep "RepositoryEntity\|User" src/main/java/com/optimetric/github/api`

### ✅ Infrastructure Layer Kuralları

- [ ] **Infrastructure → Use Case Yok**
  - `src/main/java/com/optimetric/github/infrastructure` içinde use case orkestrasyonu yok
  - Adapter'lar sadece port implement ediyor, business logic yok
  - Doğrulama: `git grep "usecase\|UseCase" src/main/java/com/optimetric/github/infrastructure`

### ✅ Port/Adapter Pattern Kuralları

- [ ] **Outbound Port'lar Application Layer'da**
  - `RepositoryPersistencePort`, `GitHubRepoQueryPort`, `UserContextPort` → `application/port/out`
  - Doğrulama: `find src/main/java/com/optimetric/github/application/port/out -name "*.java"`

- [ ] **Adapter'lar Infrastructure Layer'da**
  - `RepositoryPersistenceAdapter` → `infrastructure/persistence/adapter`
  - `GitHubRepoQueryAdapter` → `infrastructure/http/adapter`
  - Doğrulama: `find src/main/java/com/optimetric/github/infrastructure -name "*Adapter.java"`

- [ ] **Port'lar Interface, Adapter'lar Implementation**
  - Port'lar application layer'da interface
  - Adapter'lar infrastructure layer'da implementation
  - Doğrulama: `git grep "implements.*Port" src/main/java/com/optimetric/github/infrastructure`

### ✅ Use Case Pattern Kuralları

- [ ] **Use Case'ler Application Layer'da**
  - `InitializeUserRepositoriesUseCase`, `QueryRepositoriesUseCase`, `GetRepositoryForCurrentUserUseCase` → `application/usecase`
  - Doğrulama: `find src/main/java/com/optimetric/github/application/usecase -name "*.java"`

- [ ] **Controller → Use Case Çağrısı**
  - Controller'lar use case'leri çağırıyor, adapter'ları değil
  - Doğrulama: `git grep "UseCase\|ServiceAdapter" src/main/java/com/optimetric/github/api/RepoController.java`

### ✅ DTO Pattern Kuralları

- [ ] **Application DTO'lar Bağımsız**
  - `RepositoryInfo`, `RepositoryInitData`, `UserContext` → `application/dto`
  - API DTO'lardan bağımsız
  - Doğrulama: `git grep "api\.request\|api\.response" src/main/java/com/optimetric/github/application/dto`

- [ ] **API DTO'lar Sadece API Layer'da**
  - `RepoInitRequest`, `RepoDto`, `GetReposRequest` → `api/request`, `api/response`
  - Doğrulama: `find src/main/java/com/optimetric/github/api -name "*Dto.java" -o -name "*Request.java"`

### ✅ Domain Layer Kuralları

- [ ] **Domain → Spring/JPA Yok**
  - `src/main/java/com/optimetric/github/domain` içinde Spring/JPA annotation'ları yok
  - Doğrulama: `git grep "@Entity\|@Service\|@Component\|@Repository" src/main/java/com/optimetric/github/domain`

## Doğrulama Komutları

### Hızlı Doğrulama Script'leri

#### 1. Repo Slice Only Verification (Önerilen)

Sadece repo slice için kontrol yapar:

```bash
./scripts/verify-repo-slice-only.sh
```

#### 2. Full Application Layer Verification

Tüm application layer için kontrol yapar (diğer slice'lar dahil):

```bash
./scripts/verify-repo-slice-architecture.sh
```

**Not:** Full verification script'i diğer slice'lar (Language, PullRequest, Commit, vb.) için de kontrol yapar. Bu slice'lar henüz refactor edilmediği için bazı ihlaller beklenebilir. Repo slice için `verify-repo-slice-only.sh` kullanılmalıdır.

echo "=== Repo Slice Architecture Verification ==="
echo ""

echo "1. Checking Application → API imports..."
if git grep -q "com\.optimetric\.github\.api" src/main/java/com/optimetric/github/application; then
    echo "❌ FAIL: Application layer imports API layer"
    git grep "com\.optimetric\.github\.api" src/main/java/com/optimetric/github/application
    exit 1
else
    echo "✅ PASS: No API imports in application layer"
fi

echo ""
echo "2. Checking Application → Infrastructure imports..."
if git grep -q "com\.optimetric\.github\.infrastructure" src/main/java/com/optimetric/github/application; then
    echo "❌ FAIL: Application layer imports Infrastructure layer"
    git grep "com\.optimetric\.github\.infrastructure" src/main/java/com/optimetric/github/application
    exit 1
else
    echo "✅ PASS: No Infrastructure imports in application layer"
fi

echo ""
echo "3. Checking Application → Persistence Entity imports..."
if git grep -q "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/application; then
    echo "❌ FAIL: Application layer imports Persistence entities"
    git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/application
    exit 1
else
    echo "✅ PASS: No Persistence entity imports in application layer"
fi

echo ""
echo "4. Checking Application → JPA Repository imports..."
if git grep -q "RepositoryRepository" src/main/java/com/optimetric/github/application; then
    echo "❌ FAIL: Application layer imports JPA Repository"
    git grep "RepositoryRepository" src/main/java/com/optimetric/github/application
    exit 1
else
    echo "✅ PASS: No JPA Repository imports in application layer"
fi

echo ""
echo "5. Checking API → Persistence Entity imports..."
if git grep -q "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/api; then
    echo "❌ FAIL: API layer imports Persistence entities"
    git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/api
    exit 1
else
    echo "✅ PASS: No Persistence entity imports in API layer"
fi

echo ""
echo "6. Checking Infrastructure → Use Case usage..."
if git grep -q "usecase\|UseCase" src/main/java/com/optimetric/github/infrastructure; then
    echo "⚠️  WARNING: Infrastructure layer may use UseCase (check if adapter behavior only)"
    git grep "usecase\|UseCase" src/main/java/com/optimetric/github/infrastructure
else
    echo "✅ PASS: No UseCase usage in Infrastructure layer"
fi

echo ""
echo "=== All checks passed! ==="
```

### Manuel Doğrulama Komutları

```bash
# 1. Application → API import kontrolü
git grep "com\.optimetric\.github\.api" src/main/java/com/optimetric/github/application

# 2. Application → Infrastructure import kontrolü
git grep "com\.optimetric\.github\.infrastructure" src/main/java/com/optimetric/github/application

# 3. Application → Persistence Entity import kontrolü
git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/application

# 4. Application → JPA Repository import kontrolü
git grep "RepositoryRepository" src/main/java/com/optimetric/github/application

# 5. API → Persistence Entity import kontrolü
git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/api

# 6. Infrastructure → Use Case kontrolü
git grep "usecase\|UseCase" src/main/java/com/optimetric/github/infrastructure

# 7. RepositoryEntity kullanımı kontrolü (application layer'da olmamalı)
git grep "RepositoryEntity" src/main/java/com/optimetric/github/application

# 8. Port yapısı kontrolü
find src/main/java/com/optimetric/github/application/port/out -name "*.java"

# 9. Use Case yapısı kontrolü
find src/main/java/com/optimetric/github/application/usecase -name "*.java"
```

## PR Template Eklentisi

PR description'a eklenecek bölüm:

```markdown
## Architecture Compliance Checklist

- [ ] Application layer does not import API layer (`com.optimetric.github.api`)
- [ ] Application layer does not import Infrastructure layer (`com.optimetric.github.infrastructure`)
- [ ] Application layer does not import Persistence entities (`infrastructure.persistence.entity`)
- [ ] Application layer does not import JPA Repositories (`RepositoryRepository`)
- [ ] API layer does not import Persistence entities
- [ ] Infrastructure layer does not contain Use Case orchestration
- [ ] Outbound ports are in `application/port/out`
- [ ] Adapters are in `infrastructure/*/adapter`
- [ ] Use Cases are in `application/usecase`
- [ ] Controller calls Use Cases, not Adapters directly

### Verification Commands

Run these commands to verify compliance:

```bash
# Quick verification
./scripts/verify-repo-slice-architecture.sh

# Or manual checks
git grep "com\.optimetric\.github\.api" src/main/java/com/optimetric/github/application
git grep "com\.optimetric\.github\.infrastructure" src/main/java/com/optimetric/github/application
git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/application
git grep "RepositoryRepository" src/main/java/com/optimetric/github/application
```
```

## CI/CD Integration (Opsiyonel)

GitHub Actions workflow'a eklenebilir:

```yaml
name: Architecture Compliance Check

on: [pull_request]

jobs:
  architecture-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Check Application → API imports
        run: |
          if git grep -q "com\.optimetric\.github\.api" src/main/java/com/optimetric/github/application; then
            echo "❌ FAIL: Application layer imports API layer"
            git grep "com\.optimetric\.github\.api" src/main/java/com/optimetric/github/application
            exit 1
          fi
      
      - name: Check Application → Infrastructure imports
        run: |
          if git grep -q "com\.optimetric\.github\.infrastructure" src/main/java/com/optimetric/github/application; then
            echo "❌ FAIL: Application layer imports Infrastructure layer"
            git grep "com\.optimetric\.github\.infrastructure" src/main/java/com/optimetric/github/application
            exit 1
          fi
      
      - name: Check Application → Persistence Entity imports
        run: |
          if git grep -q "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/application; then
            echo "❌ FAIL: Application layer imports Persistence entities"
            git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/application
            exit 1
          fi
```

## Referanslar

- ISSUE 1: API DTO sızıntısı kesildi
- ISSUE 2: Infrastructure entity/enum sızıntısı kesildi
- ISSUE 3: RepositoryPersistencePort oluşturuldu
- ISSUE 4: GitHubRepoQueryPort oluşturuldu
- ISSUE 5: UserContextPort oluşturuldu
- ISSUE 6: Use Case'ler application layer'a taşındı
- ISSUE 7: Repository uniqueness kararı (githubRepoId)

## Notlar

- Bu guardrails repo slice için geçerlidir
- Diğer slice'lar (Branch, Commit, PullRequest, vb.) için benzer guardrails oluşturulabilir
- Checklist PR review sırasında zorunlu olmalıdır
- CI/CD integration opsiyoneldir ama önerilir
