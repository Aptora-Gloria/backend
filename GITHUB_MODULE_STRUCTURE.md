# GitHub Modülü - Güncel Paket Yapısı

## Hexagonal Architecture (Ports & Adapters) Pattern

GitHub modülü **Hexagonal Architecture** pattern'ine göre yapılandırılmış:

```
github/
│
├── api/                                    # 🔵 Adapter Layer (Inbound/Driving)
│   ├── BranchController.java
│   ├── CommitController.java
│   ├── LanguageController.java
│   ├── RepoController.java
│   │
│   ├── request/                            # API Request DTOs
│   │   ├── BranchInitRequest.java
│   │   ├── CommitInitRequest.java
│   │   ├── LanguageInitRequest.java
│   │   └── RepoInitrequest.java
│   │
│   └── response/                           # API Response DTOs
│       ├── BranchDto.java
│       ├── CommitDto.java
│       ├── LanguageDto.java
│       ├── LanguageUiDto.java
│       └── RepoDto.java
│
├── application/                            # 🟢 Application Layer (Core/Domain)
│   ├── port/                               # Port Interfaces (Outbound)
│   │   ├── ApiClientI.java                # External API client port
│   │   ├── BranchServiceI.java             # Branch service port
│   │   ├── CommitServiceI.java             # Commit service port
│   │   ├── GitHubServiceI.java             # GitHub service port
│   │   ├── LanguageServiceI.java           # Language service port
│   │   └── RepoServiceI.java               # Repository service port
│   │
│   └── service/                            # (Boş - muhtemelen domain services için)
│
└── infrastructure/                         # 🔴 Infrastructure Layer (Outbound/Driven)
    ├── config/                             # Configuration
    │   ├── GitHubOAuthProperties.java
    │   └── WebClientConfig.java
    │
    ├── constant/                           # Constants
    │   └── GitHubConstants.java
    │
    ├── enums/                              # Enumerations
    │   ├── GitHubContentType.java
    │   └── GitHubErrorCode.java
    │
    ├── http/                               # HTTP Adapters
    │   └── ApiClientImpl.java              # Implements ApiClientI port
    │
    ├── mapper/                              # Data Mappers
    │   ├── BranchMapper.java
    │   ├── CommitMapper.java
    │   ├── LanguageMapper.java
    │   └── RepoMapper.java
    │
    ├── persistence/                        # Persistence Layer
    │   ├── adapter/                        # Service Adapters (Implement Ports)
    │   │   ├── BranchServiceImpl.java      # Implements BranchServiceI
    │   │   ├── CommitServiceImpl.java      # Implements CommitServiceI
    │   │   ├── GitHubServiceImpl.java      # Implements GitHubServiceI
    │   │   ├── LanguageServiceImpl.java    # Implements LanguageServiceI
    │   │   └── RepoServiceImpl.java        # Implements RepoServiceI
    │   │
    │   ├── entity/                         # JPA Entities
    │   │   ├── BranchEntity.java
    │   │   ├── CommitEntity.java
    │   │   ├── LanguageEntity.java
    │   │   └── RepositoryEntity.java
    │   │
    │   └── jpa/                           # JPA Repositories
    │       ├── BranchRepository.java
    │       ├── CommitRepository.java
    │       ├── LanguageRepository.java
    │       └── RepositoryRepository.java
    │
    └── util/                               # Utilities
        ├── BranchUid.java
        ├── GitHubUtil.java
        └── TokenUtil.java
```

## Katman Açıklamaları

### 1. **API Layer** (Inbound Adapter)
- **Amaç**: HTTP isteklerini karşılar
- **İçerik**: Controllers ve API DTOs (request/response)
- **Bağımlılık**: `application.port.*` interface'lerine bağımlı

### 2. **Application Layer** (Core/Domain)
- **Amaç**: İş mantığı ve port tanımları
- **Ports**: Dış dünyayla iletişim için interface'ler
  - `ApiClientI`: GitHub API ile iletişim portu
  - `*ServiceI`: Domain servis portları
- **Bağımlılık**: Sadece kendi portlarına bağımlı (infrastructure'a değil)

### 3. **Infrastructure Layer** (Outbound Adapter)
- **Amaç**: Dış sistemlerle entegrasyon
- **HTTP**: GitHub API client implementasyonu
- **Persistence**: Database adapters ve entities
- **Config/Util**: Yardımcı sınıflar

## Dependency Flow (Bağımlılık Akışı)

```
API (Controllers)
    ↓
Application (Ports) ← Controllers buraya bağımlı
    ↑
Infrastructure (Adapters) ← Ports'u implement eder
```

**Kural**: 
- ✅ API → Application (ports)
- ✅ Infrastructure → Application (ports implement eder)
- ❌ Application → Infrastructure (YOK! Dependency inversion)

## Avantajlar

1. **Testability**: Ports sayesinde kolay mock'lanabilir
2. **Flexibility**: Infrastructure değişiklikleri core'u etkilemez
3. **Clean Architecture**: Dependency rule'a uygun
4. **Separation of Concerns**: Her katman kendi sorumluluğunda

## Notlar

- ⚠️ Interface'ler hala `*I` suffix kullanıyor (`ApiClientI`, `RepoServiceI`)
- ✅ Port/Adapter pattern doğru uygulanmış
- ✅ Controllers API layer'da
- ✅ Entities persistence layer'da
- ✅ Mappers infrastructure'da

## Öneriler

1. Interface isimlerinden `I` suffix'i kaldırılabilir (Java standardına uygun olması için)
2. `application.service` klasörü boş - domain services eklenebilir veya kaldırılabilir
3. Exception handling için `infrastructure/exception` klasörü eklenebilir



