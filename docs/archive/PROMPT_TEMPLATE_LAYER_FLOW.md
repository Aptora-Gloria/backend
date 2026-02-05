# Prompt Template: Katmanlar Arası Akış Modellemesi

Başka bir projede katmanlar arası akış modellemesi oluşturmak için aşağıdaki prompt'u kullanabilirsiniz:

---

## Prompt

```
[PROJE_ADI] servisi için katmanlar arası akışın modellemesini yap. Her katman için detaylı bilgileri ver.

Proje Yapısı:
- Mimari: [Hexagonal Architecture / Clean Architecture / vb.]
- Framework: [Spring Boot / Quarkus / vb.]
- Database: [PostgreSQL / MongoDB / vb.]

Katmanlar:
1. API Layer
   - Bileşenler: [Controller'lar, Presenter'lar, vb.]
   - DTOs: [Request/Response DTOs]
   
2. Application Layer
   - Use Cases: [Liste]
   - Domain Services: [Liste]
   - Ports: [Inbound/Outbound port'lar]
   - DTOs: [Application DTOs]
   
3. Domain Layer
   - Value Objects: [Liste]
   - Domain Services: [Liste]
   - Entities: [Eğer varsa]
   
4. Infrastructure Layer
   - Adapters: [Liste]
   - Mappers: [Liste]
   - Entities: [JPA entities]
   - External Clients: [HTTP clients, vb.]

Ana Akış Senaryoları:
1. [Senaryo 1 adı]
   - Entry point: [Controller method]
   - Flow: [Kısa açıklama]
   
2. [Senaryo 2 adı]
   - Entry point: [Controller method]
   - Flow: [Kısa açıklama]
   
3. [Senaryo 3 adı]
   - Entry point: [Controller method]
   - Flow: [Kısa açıklama]

Özel Kurallar:
- [Örnek: Application layer'da API import yasak]
- [Örnek: Domain layer'da framework dependency yok]
- [Örnek: Exception handling pattern]

İstenen Format:
- Mimari diyagram (ASCII art)
- Her akış için detaylı flow diagram
- Her katman için sorumluluklar ve bileşenler
- Data flow (request/response)
- Exception handling akışı
- Katman bağımlılık kuralları
```

---

## Örnek Prompt (Repo Service için kullanılan)

```
Repo servisi için katmanlar arası akışın modellemesini yap. Her katman için detaylı bilgileri ver.

Proje Yapısı:
- Mimari: Hexagonal Architecture
- Framework: Spring Boot
- Database: PostgreSQL

Katmanlar:
1. API Layer
   - Bileşenler: RepoController, RepoApiPresenter
   - DTOs: GetReposRequest, RepoDto, RepoInitRequest
   
2. Application Layer
   - Use Cases: InitializeUserRepositoriesUseCase, QueryRepositoriesUseCase, GetRepositoryForCurrentUserUseCase
   - Domain Services: RepositoryDomainService
   - Ports: UserContextPort, GitHubRepoQueryPort, RepositoryPersistencePort
   - DTOs: RepositoryInfo, RepositoryInitData, UserContext
   
3. Domain Layer
   - Value Objects: RepositoryFullName
   - Domain Services: RepositoryDomainService
   
4. Infrastructure Layer
   - Adapters: UserContextAdapter, GitHubRepoQueryAdapter, RepositoryPersistenceAdapter
   - Mappers: RepositoryMapper, ApplicationToApiMapper
   - Entities: RepositoryEntity
   - External Clients: GitHub API WebClient

Ana Akış Senaryoları:
1. Initialize User Repositories
   - Entry point: RepoController.initUserRepos()
   - Flow: GitHub API'den repo'ları çek, deduplicate et, database'e kaydet
   
2. Query Repositories
   - Entry point: RepoController.getRepos()
   - Flow: Database'den filtreleme kriterlerine göre repo'ları getir
   
3. Get Repository by FullName
   - Entry point: RepoController (via presenter)
   - Flow: FullName validate et, database'den getir, access control yap

Özel Kurallar:
- Application layer'da API import yasak
- Application layer'da Infrastructure import yasak
- Domain layer'da framework dependency yok
- Outbound port'lar exception throw etmez (Optional pattern)
- Use case'ler domain service'e delegate eder

İstenen Format:
- Mimari diyagram (ASCII art)
- Her akış için detaylı flow diagram
- Her katman için sorumluluklar ve bileşenler
- Data flow (request/response)
- Exception handling akışı
- Katman bağımlılık kuralları
```

---

## Alternatif: Daha Kısa Prompt

Eğer proje yapısı zaten kodda mevcutsa, daha kısa bir prompt da kullanabilirsiniz:

```
[Servis Adı] servisi için katmanlar arası akışın modellemesini yap. Her katman için detaylı bilgileri ver.

Kod yapısını inceleyip:
1. Tüm katmanları (API, Application, Domain, Infrastructure) analiz et
2. Her katmandaki bileşenleri (Controller, UseCase, Port, Adapter, vb.) listele
3. Ana akış senaryolarını belirle ve her biri için detaylı flow diagram oluştur
4. Her katmanın sorumluluklarını ve kurallarını belirt
5. Data flow (request/response) ve exception handling akışını göster
6. Katman bağımlılık kurallarını dokümante et

Format: REPO_SERVICE_LAYER_FLOW.md dosyasındaki gibi detaylı ve görsel bir dokümantasyon.
```

---

## İpuçları

1. **Proje yapısını paylaşın**: Katmanlar ve bileşenler hakkında bilgi verin
2. **Akış senaryolarını belirtin**: Hangi use case'ler var, nasıl çalışıyorlar
3. **Özel kuralları belirtin**: Mimari kurallar, kısıtlamalar
4. **Teknoloji stack'i belirtin**: Framework, database, vb.
5. **Referans verin**: Benzer bir dokümantasyon varsa (ör. REPO_SERVICE_LAYER_FLOW.md)

Bu bilgilerle aynı kalitede bir dokümantasyon oluşturabilirim.
