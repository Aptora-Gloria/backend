# Mimari Karşılaştırma: Repo Service vs Feed Service

> **Versiyon:** 1.0.0  
> **Tarih:** 2025-01-27

Bu dokümanda Repo Service ve Feed Service'in Hexagonal Architecture implementasyonları arasındaki farklar ve benzerlikler detaylı olarak karşılaştırılmaktadır.

---

## Genel Bakış

| Özellik | Repo Service | Feed Service |
|---------|--------------|--------------|
| **Mimari Yaklaşım** | Minimal Hexagonal | Full DDD + Hexagonal |
| **Domain Complexity** | Basit (CRUD odaklı) | Karmaşık (Business logic ağırlıklı) |
| **Domain Layer** | Minimal (VO + Domain Service) | Full DDD (Aggregates + Events) |
| **Application Pattern** | Use Case (Direct) | API Port + Use Case |
| **Event-Driven** | ❌ Yok | ✅ Domain Events + External Events |
| **Cache Strategy** | ❌ Yok | ✅ Redis (Timeline, Hashtag, Stats) |
| **Rate Limiting** | ❌ Yok | ✅ Redis-based |
| **Transaction Management** | Implicit | Explicit (@Transactional) |

---

## 1. Domain Layer Farkları

### Repo Service (Minimal Domain)

```
Domain Layer
├── Value Objects
│   └── RepositoryFullName
└── Domain Services
    └── RepositoryDomainService
        - Validation
        - Business Rules
        - Access Control
```

**Özellikler:**
- ✅ Minimal domain model
- ✅ Sadece gerekli business logic
- ✅ Value Object pattern (RepositoryFullName)
- ❌ Aggregate yok
- ❌ Domain Event yok
- ❌ Domain Exception yok (shared exception kullanıyor)

### Feed Service (Full DDD)

```
Domain Layer
├── Aggregates
│   ├── Post
│   ├── PostComment
│   ├── PostLike
│   ├── Activity
│   └── Hashtag
├── Value Objects
│   ├── PostId, PostContent, PostVisibility
│   ├── CommentText, HashtagName, MediaUrl
│   └── Mention, ActivityType
├── Domain Services
│   ├── PostService
│   ├── InteractionService
│   ├── TimelineService
│   └── StatisticsService
├── Domain Events
│   ├── PostCreatedEvent
│   ├── PostLikedEvent
│   └── PostCommentedEvent
└── Domain Exceptions
    ├── PostNotFoundException
    ├── AlreadyLikedException
    └── RateLimitExceededException
```

**Özellikler:**
- ✅ Full DDD implementation
- ✅ Aggregate root pattern
- ✅ Rich domain model
- ✅ Domain events
- ✅ Domain-specific exceptions
- ✅ Complex business rules

**Karar Kriteri:**
- **Repo Service:** CRUD odaklı, basit business logic → Minimal domain yeterli
- **Feed Service:** Karmaşık business rules, event-driven → Full DDD gerekli

---

## 2. Application Layer Farkları

### Repo Service (Use Case Direct)

```
Application Layer
├── Use Cases (Direct)
│   ├── InitializeUserRepositoriesUseCase
│   ├── QueryRepositoriesUseCase
│   └── GetRepositoryForCurrentUserUseCase
├── Domain Service
│   └── RepositoryDomainService
└── Outbound Ports
    ├── UserContextPort
    ├── GitHubRepoQueryPort
    └── RepositoryPersistencePort
```

**Pattern:**
```java
// Controller → Use Case (direct)
@RestController
public class RepoController {
    private final InitializeUserRepositoriesUseCase useCase;
    
    public ResponseEntity<?> initUserRepos(@RequestBody RepoInitRequest request) {
        List<RepositoryInitData> result = useCase.execute(login);
        return ResponseEntity.ok(presenter.toResponse(result));
    }
}
```

**Özellikler:**
- ✅ Use case'ler doğrudan inject ediliyor
- ✅ Inbound port yok (use case'ler interface değil)
- ✅ Basit orchestration
- ✅ Minimal abstraction

### Feed Service (API Port Pattern)

```
Application Layer
├── API Ports (Inbound)
│   ├── PostApiPort
│   ├── InteractionApiPort
│   ├── TimelineApiPort
│   └── StatisticsApiPort
├── API Services (Use Cases)
│   ├── PostApiService (implements PostApiPort)
│   ├── InteractionApiService (implements InteractionApiPort)
│   └── TimelineApiService (implements TimelineApiPort)
└── Outbound Ports
    ├── PostRepositoryPort
    ├── ProfileQueryPort
    └── CachePort
```

**Pattern:**
```java
// Controller → API Port (interface)
@RestController
public class PostController {
    private final PostApiPort postApiPort;
    
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request) {
        PostResult result = postApiPort.createPost(command);
        return ResponseEntity.ok(mapper.toResponse(result));
    }
}
```

**Özellikler:**
- ✅ Inbound port pattern (API Port)
- ✅ Use case'ler port interface'lerini implement ediyor
- ✅ Daha fazla abstraction
- ✅ Test edilebilirlik artışı

**Karar Kriteri:**
- **Repo Service:** Basit use case'ler, az sayıda endpoint → Direct use case yeterli
- **Feed Service:** Karmaşık API, çok sayıda endpoint → API Port pattern daha uygun

---

## 3. Infrastructure Layer Farkları

### Repo Service (Basit Infrastructure)

```
Infrastructure Layer
├── Adapters
│   ├── UserContextAdapter
│   ├── GitHubRepoQueryAdapter
│   └── RepositoryPersistenceAdapter
├── Mappers
│   ├── RepositoryMapper
│   └── ApplicationToApiMapper
├── Entities & JPA
│   ├── RepositoryEntity
│   └── RepositoryRepository
└── HTTP Clients
    └── GitHub API WebClient
```

**Özellikler:**
- ✅ Basit adapter pattern
- ✅ JPA persistence
- ✅ External API client
- ❌ Cache yok
- ❌ Event listener yok
- ❌ Rate limiting yok

### Feed Service (Kapsamlı Infrastructure)

```
Infrastructure Layer
├── Persistence Adapters
│   ├── PostRepositoryAdapter
│   ├── PostCommentRepositoryAdapter
│   └── ActivityRepositoryAdapter
├── Query Adapters
│   ├── ProfileQueryAdapter
│   └── ContentQueryAdapter
├── Cache Adapters
│   ├── CacheRedisAdapter
│   └── RateLimitRedisAdapter
├── Event Listeners
│   ├── ContentEventListener
│   └── ProfileEventListener
├── Mappers
│   ├── PostEntityMapper
│   └── ActivityEntityMapper
└── JPA Entities
    ├── PostEntity
    └── ActivityEntity
```

**Özellikler:**
- ✅ Cache adapters (Redis)
- ✅ Rate limiting adapters
- ✅ Event listeners (external events)
- ✅ Query adapters (external services)
- ✅ Kapsamlı infrastructure

**Karar Kriteri:**
- **Repo Service:** Basit CRUD, external API sadece GitHub → Minimal infrastructure
- **Feed Service:** Cache, rate limiting, event-driven → Kapsamlı infrastructure gerekli

---

## 4. Exception Handling Farkları

### Repo Service (Optional Pattern)

```java
// Outbound Port - Optional döner
public interface UserContextPort {
    Optional<UserContext> getContextByLogin(String login);
}

// Use Case - Optional kontrol eder
public class InitializeUserRepositoriesUseCase {
    public List<RepositoryInitData> execute(String login) {
        Optional<UserContext> context = userContextPort.getContextByLogin(login);
        if (context.isEmpty()) {
            throw new UserNotFoundException(login);
        }
        // ...
    }
}
```

**Pattern:**
- ✅ Outbound port'lar `Optional` döner
- ✅ Use case'ler `Optional` kontrol eder
- ✅ Business exception'lar use case'de throw edilir
- ✅ Shared exception'lar kullanılır

### Feed Service (Domain Exception Pattern)

```java
// Domain Service - Domain exception throw eder
public class PostService {
    public Post createPost(CreatePostCommand command) {
        if (rateLimitPort.checkRateLimit(userId).isExceeded()) {
            throw new RateLimitExceededException(userId);
        }
        // ...
    }
}

// Exception Handler - Domain exception'ları map eder
@ExceptionHandler(PostNotFoundException.class)
public ResponseEntity<ErrorResponse> handlePostNotFound(PostNotFoundException e) {
    return ResponseEntity.status(404).body(new ErrorResponse("POST_NOT_FOUND", e.getMessage()));
}
```

**Pattern:**
- ✅ Domain exception'lar domain layer'da tanımlanır
- ✅ Domain service'ler exception throw eder
- ✅ Exception handler domain exception'ları HTTP status'a map eder
- ✅ Domain-specific exception'lar

**Karar Kriteri:**
- **Repo Service:** Basit exception handling → Optional pattern yeterli
- **Feed Service:** Karmaşık business rules → Domain exception pattern daha uygun

---

## 5. Transaction Management Farkları

### Repo Service

**Dokümantasyonda belirtilmemiş:**
- Implicit transaction management (Spring default)
- Use case'lerde `@Transactional` annotation yok (belirtilmemiş)

### Feed Service

**Explicit transaction management:**
```java
@Transactional
public class PostApiService implements PostApiPort {
    public PostResult createPost(CreatePostCommand command) {
        // Transaction boundary açıkça belirtilmiş
    }
}

@Transactional(readOnly = true)
public class TimelineApiService implements TimelineApiPort {
    public Page<ActivityResult> getTimeline(GetTimelineQuery query) {
        // Read-only transaction
    }
}
```

**Özellikler:**
- ✅ Explicit `@Transactional` annotations
- ✅ Read-only transactions için `readOnly = true`
- ✅ Transaction boundary'ler açıkça belirtilmiş

**Karar Kriteri:**
- **Repo Service:** Basit işlemler → Implicit transaction yeterli
- **Feed Service:** Karmaşık işlemler, multiple repository calls → Explicit transaction gerekli

---

## 6. Cache Strategy

### Repo Service

**Cache yok:**
- ❌ Cache layer yok
- ❌ Her istek database'e gidiyor
- ✅ Basit ve anlaşılır

### Feed Service

**Redis cache strategy:**
```java
// Timeline cache
CachePort.getTimelineCache(userId)
CachePort.setTimelineCache(userId, activities)

// Hashtag cache
CachePort.getTrendingHashtags()
CachePort.invalidateHashtagCache(hashtag)

// Cache invalidation
PostCreatedEvent → Timeline cache invalidate
PostLikedEvent → Post cache invalidate
```

**Özellikler:**
- ✅ Timeline cache (user-based)
- ✅ Hashtag cache (trending)
- ✅ User statistics cache
- ✅ Rate limit cache
- ✅ Cache invalidation strategy

**Karar Kriteri:**
- **Repo Service:** CRUD operations, low traffic → Cache gerekli değil
- **Feed Service:** High read traffic, timeline queries → Cache kritik

---

## 7. Event-Driven Architecture

### Repo Service

**Event yok:**
- ❌ Domain event yok
- ❌ External event listener yok
- ✅ Synchronous operations

### Feed Service

**Full event-driven:**
```java
// Domain Events (publish)
PostCreatedEvent → Content Service, Notification Service
PostLikedEvent → Notification Service
PostCommentedEvent → Notification Service

// External Events (listen)
ContentPublishedEvent (Content Service) → Activity oluştur
ProfileUpdatedEvent (Profile Service) → Cache invalidate
```

**Özellikler:**
- ✅ Domain events (publish)
- ✅ External event listeners
- ✅ Async communication
- ✅ Loose coupling

**Karar Kriteri:**
- **Repo Service:** Standalone service, external integration yok → Event gerekli değil
- **Feed Service:** Multi-service integration, notifications → Event-driven gerekli

---

## 8. API Layer Farkları

### Repo Service

```
API Layer
├── RepoController
└── RepoApiPresenter
    - Request/Response mapping
    - Use case orchestration
```

**Pattern:**
- ✅ Controller + Presenter pattern
- ✅ Presenter mapping logic'i yönetiyor
- ✅ Use case'ler doğrudan çağrılıyor

### Feed Service

```
API Layer
├── Controllers
│   ├── PostController
│   ├── InteractionController
│   └── TimelineController
├── Request/Response Mappers
│   ├── PostRequestMapper
│   └── PostResponseMapper
└── Exception Handler
    └── FeedModuleExceptionHandler
```

**Pattern:**
- ✅ Multiple controllers (domain-based)
- ✅ Dedicated mappers (Request/Response)
- ✅ Module-specific exception handler
- ✅ API Port pattern kullanıyor

**Karar Kriteri:**
- **Repo Service:** Tek domain (Repo) → Tek controller + presenter yeterli
- **Feed Service:** Multiple domains (Post, Interaction, Timeline) → Domain-based controllers

---

## 9. Outbound Port Pattern Farkları

### Repo Service

```java
// Optional pattern
public interface UserContextPort {
    Optional<UserContext> getContextByLogin(String login);
}

public interface RepositoryPersistencePort {
    List<RepositoryInfo> saveAll(List<RepositoryInitData> repos, Long userId);
    Optional<RepositoryInfo> findByFullNameAndUser(String fullName, User user);
}
```

**Özellikler:**
- ✅ Optional pattern (exception throw etmez)
- ✅ Basit return types
- ✅ Application DTO'lar kullanılıyor

### Feed Service

```java
// Exception throw eden port'lar
public interface PostRepositoryPort {
    Post save(Post post);
    Optional<Post> findById(PostId id);
}

public interface RateLimitPort {
    RateLimitResult checkRateLimit(UserId userId);
}
```

**Özellikler:**
- ✅ Exception throw edebilir (domain exception)
- ✅ Domain objects dönebilir (Post, Activity)
- ✅ Result objects (RateLimitResult)

**Karar Kriteri:**
- **Repo Service:** Basit operations → Optional pattern yeterli
- **Feed Service:** Karmaşık operations → Exception pattern daha uygun

---

## 10. Mapping Strategy Farkları

### Repo Service

```
Mapping Layers:
1. API → Application: RepoApiPresenter
2. Application → Domain: Use Case (direct)
3. Domain → Infrastructure: RepositoryMapper
4. Infrastructure → Application: RepositoryMapper
```

**Özellikler:**
- ✅ Minimal mapping layers
- ✅ Presenter API ↔ Application mapping
- ✅ Mapper Application ↔ Infrastructure mapping

### Feed Service

```
Mapping Layers:
1. API → Application: RequestMapper
2. Application → Domain: API Service (direct)
3. Domain → Application: ResultMapper
4. Application → API: ResponseMapper
5. Domain → Infrastructure: EntityMapper
6. Infrastructure → Domain: EntityMapper
```

**Özellikler:**
- ✅ Dedicated mappers (Request/Response)
- ✅ Result mappers (Domain → Application)
- ✅ Entity mappers (Domain ↔ Infrastructure)
- ✅ Daha fazla mapping layer

**Karar Kriteri:**
- **Repo Service:** Basit DTO'lar → Minimal mapping yeterli
- **Feed Service:** Karmaşık domain objects → Dedicated mappers gerekli

---

## Özet: Ne Zaman Hangi Yaklaşım?

### Repo Service Yaklaşımı (Minimal) Kullan:

✅ **Kriterler:**
- CRUD odaklı işlemler
- Basit business logic
- Az sayıda use case
- External service integration minimal
- Cache gerekli değil
- Event-driven gerekli değil
- Hızlı geliştirme öncelikli

**Örnek Senaryolar:**
- Repository management
- User profile management
- Simple CRUD services
- Data synchronization services

### Feed Service Yaklaşımı (Full DDD) Kullan:

✅ **Kriterler:**
- Karmaşık business rules
- Rich domain model
- Event-driven architecture
- High read traffic (cache gerekli)
- Multiple external service integration
- Rate limiting gerekli
- Domain events gerekli
- Long-term maintainability öncelikli

**Örnek Senaryolar:**
- Social media feeds
- E-commerce order management
- Financial transaction services
- Complex workflow services

---

## Sonuç

Her iki yaklaşım da Hexagonal Architecture prensiplerine uygundur, ancak farklı complexity seviyelerine göre optimize edilmiştir:

1. **Repo Service:** Minimal, pragmatik yaklaşım - gerektiği kadar domain complexity
2. **Feed Service:** Full DDD yaklaşımı - karmaşık business logic için gerekli tüm pattern'ler

**Ana Prensip:** Complexity'e göre mimari seçimi yapılmalı. Basit işlemler için minimal yaklaşım, karmaşık işlemler için full DDD yaklaşımı tercih edilmelidir.
