# Mevcut Klasör Yapısı

```
src/main/java/com/oobeya/
│
├── BackendApplication.java
│
├── auth/                          # Authentication & Authorization Modülü
│   ├── config/
│   │   └── SecurityConfig.java
│   │
│   ├── controller/
│   │   └── AuthController.java
│   │
│   ├── dto/
│   │   ├── AuthRequest.java
│   │   ├── AuthResponse.java
│   │   ├── GitHubAccessTokenResponse.java
│   │   ├── GitHubUserDto.java
│   │   └── GitHubUserResponse.java
│   │
│   ├── entity/
│   │   ├── OwnerType.java
│   │   └── User.java
│   │
│   ├── mapper/
│   │   └── GitHubUserMapper.java
│   │
│   ├── repository/
│   │   └── UserRepository.java
│   │
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   └── jwtservice/
│   │       ├── JwtService.java
│   │       └── impl/
│   │           └── JwtServiceImpl.java
│   │
│   └── service/
│       ├── AuthService.java                    # Interface
│       ├── JwtServicePort.java                 # Port interface
│       ├── UserServicePort.java                # Port interface
│       └── impl/
│           ├── AuthServiceImpl.java
│           ├── JwtServiceAdapter.java
│           └── UserServiceAdapter.java
│
└── github/                        # GitHub API Integration Modülü
    ├── config/
    │   ├── GitHubOAuthProperties.java
    │   └── WebClientConfig.java
    │
    ├── constant/
    │   └── GitHubConstants.java
    │
    ├── controller/
    │   ├── BranchController.java
    │   ├── CommitController.java
    │   ├── LanguageController.java
    │   └── RepoController.java
    │
    ├── dto/
    │   ├── request/
    │   │   ├── BranchInitRequest.java
    │   │   ├── CommitInitRequest.java
    │   │   ├── LanguageInitRequest.java
    │   │   └── RepoInitrequest.java
    │   │
    │   └── response/
    │       ├── BranchDto.java
    │       ├── CommitDto.java
    │       ├── LanguageDto.java
    │       ├── LanguageUiDto.java
    │       └── RepoDto.java
    │
    ├── entity/
    │   ├── BranchEntity.java
    │   ├── CommitEntity.java
    │   ├── LanguageEntity.java
    │   └── RepositoryEntity.java
    │
    ├── enums/
    │   ├── GitHubContentType.java
    │   └── GitHubErrorCode.java
    │
    ├── exception/
    │   ├── GitHubApiException.java
    │   └── GlobalExceptionHandler.java
    │
    ├── mapper/
    │   ├── BranchMapper.java
    │   ├── CommitMapper.java
    │   ├── LanguageMapper.java
    │   └── RepoMapper.java
    │
    ├── repository/
    │   ├── BranchRepository.java
    │   ├── CommitRepository.java
    │   ├── LanguageRepository.java
    │   └── RepositoryRepository.java
    │
    ├── service/
    │   ├── ApiClientI.java                      # Interface (I suffix)
    │   ├── BranchServiceI.java                  # Interface (I suffix)
    │   ├── CommitServiceI.java                  # Interface (I suffix)
    │   ├── GitHubServiceI.java                  # Interface (I suffix)
    │   ├── LanguageServiceI.java                # Interface (I suffix)
    │   ├── RepoServiceI.java                    # Interface (I suffix)
    │   │
    │   └── impl/
    │       ├── ApiClientImpl.java
    │       ├── BranchServiceImpl.java
    │       ├── CommitServiceImpl.java
    │       ├── GitHubServiceImpl.java
    │       ├── LanguageServiceImpl.java
    │       └── RepoServiceImpl.java
    │
    └── util/
        ├── BranchUid.java
        ├── GitHubUtil.java
        └── TokenUtil.java
```

## Özet İstatistikler

- **Toplam Java Dosyası**: 68
- **Auth Modülü**: 19 dosya
- **GitHub Modülü**: 48 dosya
- **Ana Uygulama**: 1 dosya

## Modül Yapısı

### Auth Modülü
- ✅ Port/Adapter Pattern kullanıyor
- ✅ JWT servisi ayrılmış
- ✅ User repository pattern
- ✅ Security config mevcut

### GitHub Modülü
- ⚠️ Interface'ler `*I` suffix kullanıyor (Java standardına uygun değil)
- ⚠️ `ApiClientI` ve `GitHubServiceI` var (biraz redundancy var)
- ✅ Controller/Service/Repository/Mapper pattern kullanılıyor
- ✅ DTO'lar request/response olarak ayrılmış
- ✅ Exception handling mevcut

## Notlar

1. **Interface İsimlendirme**: Tüm GitHub service interface'leri `*I` suffix kullanıyor (örn: `RepoServiceI`, `ApiClientI`)
2. **GitHubServiceI**: `GitHubServiceImpl` wrapper olarak `ApiClientI`'yi kullanıyor
3. **Auth Modülü**: Clean architecture ile Port/Adapter pattern kullanıyor
4. **TokenService**: Auth modülünde `TokenService` interface'i yok (muhtemelen silindi veya başka yerde)


