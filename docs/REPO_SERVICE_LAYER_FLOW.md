# Repo Service - Katmanlar Arası Akış Modeli

## Genel Mimari Yapı

```
┌─────────────────────────────────────────────────────────────────┐
│                         API LAYER                               │
│  ┌──────────────────┐         ┌──────────────────────────┐    │
│  │ RepoController   │────────▶│ RepoApiPresenter         │    │
│  │ (HTTP Boundary)  │         │ (Presentation Logic)     │    │
│  └──────────────────┘         └──────────────────────────┘    │
│         │                                 │                      │
│         │ API DTOs                        │ Application DTOs     │
│         ▼                                 ▼                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ Application Contracts
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      APPLICATION LAYER                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    USE CASES                              │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │ InitializeUserRepositoriesUseCase                 │  │  │
│  │  │ QueryRepositoriesUseCase                           │  │  │
│  │  │ GetRepositoryForCurrentUserUseCase                 │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    DOMAIN SERVICE                       │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │ RepositoryDomainService                            │  │  │
│  │  │ - Validation                                       │  │  │
│  │  │ - Business Rules                                   │  │  │
│  │  │ - Access Control                                   │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    OUTBOUND PORTS                        │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │  │
│  │  │UserContext   │  │GitHubRepo    │  │Repository    │  │  │
│  │  │Port          │  │QueryPort     │  │Persistence   │  │  │
│  │  │              │  │              │  │Port         │  │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    APPLICATION DTOs                      │  │
│  │  RepositoryInfo, RepositoryInitData, UserContext         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ Port Contracts
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    VALUE OBJECTS                          │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │ RepositoryFullName                                 │  │  │
│  │  │ - Format validation                                │  │  │
│  │  │ - Immutable value object                           │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    DOMAIN SERVICE                        │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │ RepositoryDomainService                            │  │  │
│  │  │ - Pure business logic                              │  │  │
│  │  │ - No framework dependencies                        │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ Port Implementation
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    ADAPTERS                             │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │  │
│  │  │UserContext   │  │GitHubRepo    │  │Repository    │ │  │
│  │  │Adapter       │  │QueryAdapter   │  │Persistence   │ │  │
│  │  │              │  │              │  │Adapter       │ │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    MAPPERS                               │  │
│  │  RepositoryMapper, ApplicationToApiMapper                │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    ENTITIES & JPA                        │  │
│  │  RepositoryEntity, RepositoryRepository                  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    HTTP CLIENTS                          │  │
│  │  GitHub API WebClient, DTOs                              │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Akış Senaryoları

### 1. Initialize User Repositories Akışı

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP POST /api/github/repos/init/user
       ▼
┌─────────────────────────────────────────────────────────────────┐
│ API LAYER                                                        │
│ ┌──────────────────┐                                            │
│ │ RepoController   │                                            │
│ │ - Receives login │                                            │
│ │ - Validates auth │                                            │
│ └────────┬─────────┘                                            │
│          │                                                       │
│          │ presenter.initUserRepos(login)                        │
│          ▼                                                       │
│ ┌──────────────────┐                                            │
│ │ RepoApiPresenter │                                            │
│ │ - Calls use case │                                            │
│ │ - Maps to API DTO│                                            │
│ └────────┬─────────┘                                            │
└──────────┼───────────────────────────────────────────────────────┘
           │ Application Contract
           ▼
┌─────────────────────────────────────────────────────────────────┐
│ APPLICATION LAYER                                                │
│ ┌────────────────────────────────────────────────────────────┐ │
│ │ InitializeUserRepositoriesUseCase                          │ │
│ │                                                             │ │
│ │ 1. userContextPort.getContextByLogin(login)                │ │
│ │    → Returns: Optional<UserContext>                         │ │
│ │                                                             │ │
│ │ 2. githubRepoQueryPort.getUserRepos(accessToken)           │ │
│ │    → Returns: List<RepositoryInitData>                      │ │
│ │                                                             │ │
│ │ 3. repositoryPersistencePort.saveAll(repos, userId)         │ │
│ │    → Returns: List<RepositoryInfo>                          │ │
│ │                                                             │ │
│ │ Returns: List<RepositoryInitData>                           │ │
│ └────────────────────────────────────────────────────────────┘ │
└──────────┼───────────────────────────────────────────────────────┘
           │ Port Calls
           ▼
┌─────────────────────────────────────────────────────────────────┐
│ INFRASTRUCTURE LAYER                                             │
│                                                                  │
│ ┌────────────────────────────────────────────────────────────┐ │
│ │ UserContextAdapter                                          │ │
│ │ - Calls AuthServicePort.findByLoginOrThrow()                │ │
│ │ - Returns: Optional<UserContext>                            │ │
│ └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ ┌────────────────────────────────────────────────────────────┐ │
│ │ GitHubRepoQueryAdapter                                      │ │
│ │ - Calls GitHub API via WebClient                            │ │
│ │ - Maps GitHub DTO → RepositoryInitData                      │ │
│ │ - Returns: List<RepositoryInitData>                         │ │
│ └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ ┌────────────────────────────────────────────────────────────┐ │
│ │ RepositoryPersistenceAdapter                                │ │
│ │ - Deduplication by githubRepoId (canonical key)            │ │
│ │ - Upsert logic (update existing, create new)               │ │
│ │ - Maps RepositoryInitData → RepositoryEntity                │ │
│ │ - Saves via RepositoryRepository                           │ │
│ │ - Maps RepositoryEntity → RepositoryInfo                   │ │
│ │ - Returns: List<RepositoryInfo>                            │ │
│ └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 2. Query Repositories Akışı

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP POST /api/github/repos/query
       │ Body: { ownerType?, ownerLogin? }
       ▼
┌─────────────────────────────────────────────────────────────────┐
│ API LAYER                                                        │
│ ┌──────────────────┐                                            │
│ │ RepoController   │                                            │
│ │ - Validates @Valid│                                           │
│ │ - Extracts login  │                                            │
│ └────────┬─────────┘                                            │
│          │                                                       │
│          │ presenter.getRepos(login, request)                   │
│          ▼                                                       │
│ ┌──────────────────┐                                            │
│ │ RepoApiPresenter │                                            │
│ │ - Extracts params│                                            │
│ │ - Calls use case │                                            │
│ │ - Maps to API DTO│                                            │
│ └────────┬─────────┘                                            │
└──────────┼───────────────────────────────────────────────────────┘
           │ Application Contract
           ▼
┌─────────────────────────────────────────────────────────────────┐
│ APPLICATION LAYER                                                │
│ ┌────────────────────────────────────────────────────────────┐ │
│ │ QueryRepositoriesUseCase                                    │ │
│ │                                                             │ │
│ │ 1. authServicePort.findByLoginOrThrow(login)                │ │
│ │    → Returns: User entity                                   │ │
│ │                                                             │ │
│ │ 2. repositoryPersistencePort.findByUserWithCriteria(       │ │
│ │      user, ownerType, ownerLogin)                           │ │
│ │    → Returns: List<RepositoryInfo>                          │ │
│ │                                                             │ │
│ │ Returns: List<RepositoryInfo>                               │ │
│ └────────────────────────────────────────────────────────────┘ │
└──────────┼───────────────────────────────────────────────────────┘
           │ Port Calls
           ▼
┌─────────────────────────────────────────────────────────────────┐
│ INFRASTRUCTURE LAYER                                             │
│ ┌────────────────────────────────────────────────────────────┐ │
│ │ RepositoryPersistenceAdapter                                │ │
│ │ - Calls RepositoryRepository with criteria                 │ │
│ │ - Maps RepositoryEntity → RepositoryInfo                   │ │
│ │ - Returns: List<RepositoryInfo>                            │ │
│ └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 3. Get Repository by FullName Akışı

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP POST /api/github/repos/query/single
       │ Body: { fullName: "owner/repo" }
       ▼
┌─────────────────────────────────────────────────────────────────┐
│ API LAYER                                                        │
│ ┌──────────────────┐                                            │
│ │ RepoController   │                                            │
│ │ (via presenter)  │                                            │
│ └────────┬─────────┘                                            │
└──────────┼───────────────────────────────────────────────────────┘
           │ Application Contract
           ▼
┌─────────────────────────────────────────────────────────────────┐
│ APPLICATION LAYER                                                │
│ ┌────────────────────────────────────────────────────────────┐ │
│ │ GetRepositoryForCurrentUserUseCase                         │ │
│ │                                                             │ │
│ │ 1. repositoryDomainService.validateAndParseFullName(       │ │
│ │      fullName)                                              │ │
│ │    → Validates format via RepositoryFullName VO            │ │
│ │    → Throws IllegalArgumentException if invalid             │ │
│ │                                                             │ │
│ │ 2. authServicePort.findByLoginOrThrow(login)               │ │
│ │    → Returns: User entity                                   │ │
│ │                                                             │ │
│ │ 3. repositoryPersistencePort.findByFullNameAndUser(        │ │
│ │      fullName, user)                                        │ │
│ │    → Returns: Optional<RepositoryInfo>                     │ │
│ │                                                             │ │
│ │ 4. repositoryDomainService.canUserAccessRepository(        │ │
│ │      login, repository)                                    │ │
│ │    → Business rule: owner can access, public repos         │ │
│ │    → Returns: boolean                                       │ │
│ │                                                             │ │
│ │ Returns: Optional<RepositoryInfo>                          │ │
│ └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ ┌────────────────────────────────────────────────────────────┐ │
│ │ DOMAIN LAYER                                                │ │
│ │ ┌────────────────────────────────────────────────────────┐ │ │
│ │ │ RepositoryDomainService                                 │ │ │
│ │ │ - validateAndParseFullName: Uses RepositoryFullName VO  │ │ │
│ │ │ - canUserAccessRepository: Business rules               │ │ │
│ │ └────────────────────────────────────────────────────────┘ │ │
│ │ ┌────────────────────────────────────────────────────────┐ │ │
│ │ │ RepositoryFullName (Value Object)                      │ │ │
│ │ │ - Format validation                                    │ │ │
│ │ │ - Immutable                                            │ │ │
│ │ └────────────────────────────────────────────────────────┘ │ │
│ └────────────────────────────────────────────────────────────┘ │
└──────────┼───────────────────────────────────────────────────────┘
           │ Port Calls
           ▼
┌─────────────────────────────────────────────────────────────────┐
│ INFRASTRUCTURE LAYER                                             │
│ ┌────────────────────────────────────────────────────────────┐ │
│ │ RepositoryPersistenceAdapter                                │ │
│ │ - Calls RepositoryRepository.findByFullNameAndUser()       │ │
│ │ - Maps RepositoryEntity → RepositoryInfo                   │ │
│ │ - Returns: Optional<RepositoryInfo>                        │ │
│ └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Katman Detayları

### 1. API Layer

**Bileşenler:**
- `RepoController`: HTTP boundary, sadece HTTP istekleri alır/döndürür
- `RepoApiPresenter`: Presentation logic, mapping sorumluluğu

**Sorumluluklar:**
- HTTP request/response handling
- Input validation (@Valid)
- Authentication (@AuthenticationPrincipal)
- API DTO ↔ Application DTO mapping

**Kullanılan DTOs:**
- `GetReposRequest` (API request)
- `RepoDto` (API response)
- `RepoInitRequest` (API response)

**Bağımlılıklar:**
- Application layer (use cases, application DTOs)
- Infrastructure layer (mappers)

**Kurallar:**
- ❌ Persistence entity import yasak
- ❌ Business logic yok
- ✅ Sadece HTTP boundary

---

### 2. Application Layer

#### 2.1 Use Cases

**Bileşenler:**
- `InitializeUserRepositoriesUseCase`
- `QueryRepositoriesUseCase`
- `GetRepositoryForCurrentUserUseCase`

**Sorumluluklar:**
- Business flow orchestration
- Port'ları kullanarak akış yönetimi
- Exception handling (business exceptions)
- Domain service'e delegate etme

**Kullanılan Ports:**
- **Inbound:** Yok (use case'ler doğrudan çağrılıyor)
- **Outbound:**
  - `UserContextPort`
  - `GitHubRepoQueryPort`
  - `RepositoryPersistencePort`

**Kullanılan DTOs:**
- `RepositoryInfo` (application output)
- `RepositoryInitData` (application input/output)
- `UserContext` (application DTO)

**Bağımlılıklar:**
- Domain layer (domain service, value objects)
- Outbound ports (infrastructure adapters)

**Kurallar:**
- ❌ API layer import yasak
- ❌ Infrastructure layer import yasak
- ❌ Persistence entity import yasak
- ✅ Sadece application contracts

#### 2.2 Domain Service (Application Layer'da kullanılıyor)

**Bileşen:**
- `RepositoryDomainService`

**Sorumluluklar:**
- Business rule enforcement
- Domain validation
- Access control rules
- Pure business logic

**Kullanılan Domain Objects:**
- `RepositoryFullName` (value object)
- `RepositoryInfo` (application DTO)

**Bağımlılıklar:**
- Domain layer (value objects)
- Application DTOs

**Kurallar:**
- ❌ Framework dependencies yok
- ❌ Infrastructure dependencies yok
- ✅ Pure business logic

#### 2.3 Outbound Ports

**Bileşenler:**
- `UserContextPort`
- `GitHubRepoQueryPort`
- `RepositoryPersistencePort`

**Sorumluluklar:**
- Infrastructure abstraction
- Contract definition
- Application layer'ın infrastructure'dan bağımsızlığı

**Kurallar:**
- ❌ Exception throw etmez (Optional/Result pattern)
- ❌ Infrastructure types yok
- ✅ Sadece application contracts

---

### 3. Domain Layer

**Bileşenler:**
- `RepositoryFullName` (Value Object)
- `RepositoryDomainService` (Domain Service)

**Sorumluluklar:**
- Business invariants
- Domain validation
- Business rules
- Value object encapsulation

**RepositoryFullName Value Object:**
- Format validation ("owner/repo")
- Immutable
- Business invariant enforcement

**RepositoryDomainService:**
- Pure business logic
- No framework dependencies
- Business rule enforcement

**Kurallar:**
- ❌ Spring/JPA annotations yok
- ❌ Framework dependencies yok
- ✅ Pure domain logic

---

### 4. Infrastructure Layer

#### 4.1 Adapters

**Bileşenler:**
- `UserContextAdapter` → implements `UserContextPort`
- `GitHubRepoQueryAdapter` → implements `GitHubRepoQueryPort`
- `RepositoryPersistenceAdapter` → implements `RepositoryPersistencePort`

**Sorumluluklar:**
- Port implementation
- Infrastructure-specific logic
- Entity ↔ DTO mapping
- External API calls

**UserContextAdapter:**
- Calls `AuthServicePort`
- Maps to `UserContext` DTO
- Returns `Optional<UserContext>`

**GitHubRepoQueryAdapter:**
- Calls GitHub API via WebClient
- Maps GitHub DTO → `RepositoryInitData`
- Error handling

**RepositoryPersistenceAdapter:**
- JPA operations
- Deduplication logic (githubRepoId)
- Upsert logic
- Entity ↔ DTO mapping

**Kurallar:**
- ✅ Port'ları implement eder
- ✅ Infrastructure concerns (JPA, HTTP)
- ❌ Business logic orchestration yok

#### 4.2 Mappers

**Bileşenler:**
- `RepositoryMapper`: Application DTO ↔ Entity
- `ApplicationToApiMapper`: Application DTO ↔ API DTO

**Sorumluluklar:**
- Data transformation
- Field mapping
- Type conversion

**Kurallar:**
- ✅ Sadece mapping logic
- ❌ Business logic yok

#### 4.3 Entities & JPA

**Bileşenler:**
- `RepositoryEntity`: JPA entity
- `RepositoryRepository`: JPA repository

**Sorumluluklar:**
- Database persistence
- Entity relationships
- JPA operations

**Kurallar:**
- ✅ Infrastructure layer'da kalır
- ❌ Application layer'da görünmez

---

## Data Flow Özeti

### Request Flow (API → Infrastructure)

```
Client Request
    ↓
RepoController (API)
    ↓
RepoApiPresenter (API - mapping)
    ↓
UseCase (Application - orchestration)
    ↓
DomainService (Domain - validation/rules)
    ↓
Outbound Port (Application - contract)
    ↓
Adapter (Infrastructure - implementation)
    ↓
External System / Database
```

### Response Flow (Infrastructure → API)

```
External System / Database
    ↓
Adapter (Infrastructure - maps to application DTO)
    ↓
Outbound Port (Application - returns application DTO)
    ↓
UseCase (Application - returns application DTO)
    ↓
RepoApiPresenter (API - maps to API DTO)
    ↓
RepoController (API - returns HTTP response)
    ↓
Client Response
```

## Katman Bağımlılık Kuralları

```
API Layer
    ↓ (depends on)
Application Layer
    ↓ (depends on)
Domain Layer
    ↑ (depends on)
Infrastructure Layer
```

**Kurallar:**
- ✅ API → Application (use cases)
- ✅ Application → Domain (domain service)
- ✅ Application → Infrastructure (ports only)
- ✅ Infrastructure → Application (port implementation)
- ❌ Application → API (yasak)
- ❌ Application → Infrastructure (direct, yasak - sadece port)
- ❌ Domain → Infrastructure (yasak)
- ❌ Domain → Application (yasak)

## Exception Handling Akışı

```
Infrastructure Adapter
    ↓ (throws infrastructure exception)
    ↓ (catches, returns Optional.empty())
Outbound Port
    ↓ (returns Optional)
UseCase
    ↓ (checks Optional, throws business exception)
Domain Service
    ↓ (validates, throws IllegalArgumentException)
UseCase
    ↓ (catches, throws business exception)
API Presenter/Controller
    ↓ (catches, maps to HTTP error response)
Client
```

## Önemli Notlar

1. **Canonical Key:** `githubRepoId` - Repository uniqueness için kullanılıyor
2. **Upsert Logic:** `RepositoryPersistenceAdapter.saveAll()` - githubRepoId bazlı
3. **Domain Validation:** `RepositoryDomainService` - Business rules
4. **Access Control:** Domain service'de business rules, infrastructure'da permission check
5. **Mapping:** Her katman kendi mapping'ini yapar (API ↔ Application ↔ Infrastructure)
