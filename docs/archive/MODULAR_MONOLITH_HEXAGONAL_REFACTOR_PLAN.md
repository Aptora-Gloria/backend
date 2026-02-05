# Modüler Monolith + Hexagonal + SOLID Refactor Planı

Bu doküman, projedeki **tüm modüllerin** (auth, github, metrics) **modüler monolith**, **hexagonal mimari** ve **SOLID** ilkeleri açısından incelenmesini ve standart bir yapıya getirilmesi için refactor planını içerir.

---

## Tamamlanma durumu (güncel)

**Özet tablodaki 10 madde tamamlandı.** Katman bağımlılıkları hedeflenen kurallara uyuyor:

- **GitHub infrastructure** api.request/response kullanmıyor.
- **Metrics infrastructure** api.request/response kullanmıyor.
- **Application** katmanı infrastructure'a bağımlı değil (port'lar application DTO kullanıyor).
- **Auth** UserServicePort `UserInfo` döndürüyor; **GitHub** PullRequestClientPort application DTO kullanıyor.

Ek olarak: duplicate HTTP adapter'lar kaldırıldı, RepoMapper (dead code) silindi, ApplicationToApiMapper → RepoApiMapper (api/mapper) taşındı.

---

## 1. Hedef: Standart Modül Yapısı

Tüm modüller aşağıdaki paket yapısına ve kurallara uyacak.

### 1.1 Standart Paket Yapısı

```
com.optimetric.<modul>/
├── api/                          # Giriş noktası (sadece HTTP al/ver)
│   ├── <X>Controller.java
│   ├── request/                  # API request DTO'ları
│   ├── response/                 # API response DTO'ları
│   └── mapper/                   # Application DTO ↔ API DTO (sadece bu katmanda)
├── application/
│   ├── dto/                      # Application DTO'lar (Info, Command, Result)
│   ├── port/
│   │   ├── in/                   # Inbound port'lar (use case arayüzleri)
│   │   └── out/                  # Outbound port'lar (veri/dış servis arayüzleri)
│   └── usecase/                  # Use case sınıfları (port/in implement eder)
├── domain/                       # İş kuralları, saf Java, anotasyon yok
│   ├── model/ | entity/ | vo/
│   ├── enumeration/
│   └── service/
└── infrastructure/
    ├── persistence/              # Veritabanı adaptörleri
    │   ├── adapter/
    │   ├── entity/               # JPA entity (sadece infrastructure'da)
    │   └── jpa/
    ├── http/                     # HTTP client adaptörleri (varsa)
    └── core/                     # Config, mapper (entity ↔ application DTO), util
```

### 1.2 Bağımlılık Kuralları (Hexagonal)


| Kaynak katman      | Bağımlı olabileceği                        | Bağımlı olamayacağı                          |
| ------------------ | ------------------------------------------ | -------------------------------------------- |
| **api**            | application (port/in, dto), api/mapper     | infrastructure, domain (doğrudan)            |
| **application**    | domain, application/dto, port/in, port/out | api, infrastructure                          |
| **domain**         | — (sadece java.*, shared.domain)           | api, application (dto hariç), infrastructure |
| **infrastructure** | application (port/out, dto), domain        | api (request/response DTO)                   |


- **Port'lar (in/out)** sadece **application.dto** veya **domain** tipleri kullanır; **api.response/request** veya **infrastructure.entity** kullanmaz.
- **Infrastructure mapper'lar** entity ↔ **application.dto** dönüşümü yapar; **api.response** kullanmaz.

### 1.3 Modüler Monolith – Modüller Arası İletişim

- **A modülü**, B modülüne **sadece** B'nin **application** katmanı üzerinden erişir:
  - B'nin **port/in** (use case arayüzleri) veya
  - B'nin **application.dto** tipleri.
- **Yasak:** A'nın B'nin **api** veya **infrastructure** paketine (entity, JPA repository, adapter sınıfı) bağımlılığı.

### 1.4 SOLID Özeti


| İlke                          | Uygulama                                                                                                                     |
| ----------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| **S** (Single Responsibility) | Her port tek bir sorumluluk; use case sadece orkestrasyon/validasyon.                                                        |
| **O** (Open/Closed)           | Yeni davranış yeni use case / adapter ile; mevcut port imzası değişmeden.                                                    |
| **L** (Liskov)                | Port implementasyonları (adapter'lar) birbirinin yerine kullanılabilir.                                                      |
| **I** (Interface Segregation) | Port/in use case başına ince arayüz (GetBranchesPort, InitBranchesPort); tek kalın "ServicePort" yerine.                     |
| **D** (Dependency Inversion)  | Application port arayüzlerine (in/out) bağımlı; infrastructure port'ları implement eder. Port tipleri application/domain'de. |


---

## 2. Mevcut Durum Özeti ve İhlaller

### 2.1 Auth Modülü


| Konu         | Durum                                                                                    | İhlal                                                                                                           |
| ------------ | ---------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| Paket yapısı | api, application, domain, infrastructure var                                             | application/port **in** ve **out** ayrımı net değil; port'lar `application/port/out` altında, use case'ler var. |
| Port tipleri | AuthServicePort application.dto (LoginCommand, LoginResult, UserInfo) kullanıyor         | ✅ Port API DTO kullanmıyor.                                                                                     |
| OwnerType    | Hem `auth.infrastructure.persistence.entity.OwnerType` hem `shared.domain.OwnerType` var | Duplikasyon; tek kaynak `shared.domain.OwnerType` olmalı, auth persistence entity'si shared kullanmalı.         |
| Cross-module | GitHub, Auth'ın AuthServicePort'unu kullanıyor                                           | ✅ Doğru. Ancak GitHub **ek olarak** auth'ın User entity ve UserRepository'sini doğrudan kullanıyor (aşağıda).   |


**Öneri:** Auth için port/in ve port/out ayrımını netleştir; `auth.infrastructure.persistence.entity.OwnerType` kaldır, sadece `shared.domain.OwnerType` kullan.

---

### 2.2 GitHub Modülü


| Konu                                   | Durum                                                                                                                              | İhlal                                                                                                                                                                                  |
| -------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| İkili port yapısı                      | Hem **port/in** (GetBranchesPort, InitBranchesPort, …) hem **eski service port'lar** (BranchServicePort, CommitServicePort, …) var | BranchServicePort **api.response.BranchDto** dönüyor (DIP). Controller'lar artık port/in kullanıyorsa eski service port'lar kaldırılmalı.                                              |
| Outbound port tipleri                  | CommitClientPort, LanguageClientPort **api.response.CommitDto / LanguageDto** dönüyor                                              | Outbound port **application DTO** veya infrastructure'a özel DTO (http.dto) kullanmalı; api katmanına bağımlı olmamalı.                                                                |
| AuthClientPort / GitHubServiceAdapter  | Auth modülünün **api.dto** (GitHubAccessTokenResponse, GitHubUserDto) kullanılıyor                                                 | GitHub → Auth iletişimi Auth **application** port + application DTO ile yapılmalı.                                                                                                     |
| PullRequestServiceAdapter              | `UserRepository` ve `User` entity (auth infrastructure) doğrudan kullanılıyor                                                      | **Modüler monolith ihlali.** User bilgisi Auth'ın **application port**'u (örn. AuthServicePort.findByLoginOrThrow) ile alınmalı; entity/repository import'u kaldırılmalı.              |
| Infrastructure mapper'lar              | BranchMapper, CommitMapper, LanguageMapper, PullRequestMapper **api.response** DTO kullanıyor                                      | Entity → **application.dto** map edilmeli; API DTO'ya dönüşüm **api/mapper** katmanında yapılmalı.                                                                                     |
| DataLevel / SyncJobStatus, SyncJobType | domain.enumeration ve infrastructure.persistence.entity içinde benzer enum'lar                                                     | Tek kaynak **domain** olmalı; persistence entity enum'u kaldırılıp domain enum kullanılmalı (veya persistence entity kendi enum'unu sadece JPA için tutar, adapter domain'e map eder). |
| SyncJobServicePort                     | SyncJobEntity, SyncJobStatus, SyncJobType (infrastructure) kullanıyor                                                              | Port imzası application/dto veya domain tipi kullanmalı.                                                                                                                               |


**Öneri:**  

- Tüm outbound port'ları application DTO (veya http.dto) ile çalışacak şekilde değiştir; infrastructure mapper entity → application.dto yapsın; api mapper application.dto → api.response yapsın.  
- GitHub → Auth: UserRepository / User kullanımını kaldır; sadece AuthServicePort (ve gerekirse Auth'da getUserByLogin gibi bir application port) kullan.  
- Eski BranchServicePort, CommitServicePort vb. kullanıımı kaldırıldıysa interface'leri sil; kullanılıyorsa port/in + application DTO'ya geçir.

---

### 2.3 Metrics Modülü


| Konu                                                  | Durum                                                                                                                  | İhlal                                                                                                                               |
| ----------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| RepositoryMetricsServicePort / UserMetricsServicePort | **api.response.*** (RepositoryMetricsDto, RepositoryOverviewDto, …) dönüyor                                            | Application port **application.dto** (Info) dönmeli; API DTO'ya dönüşüm controller + api mapper'da yapılmalı.                       |
| Controller                                            | Bazı endpoint'ler doğrudan RepositoryMetricsServicePort'tan API DTO alıyor; bazıları GetXxxPort + ApiMapper kullanıyor | Tutarsızlık. Standart: Tüm endpoint'ler port/in (veya tek GetRepositoryMetricsPort) + Application DTO → ApiMapper → API DTO.        |
| Infrastructure adapter                                | RepositoryMetricsServiceAdapter, UserMetricsServiceAdapter **api.response** ve **api.mapper** import ediyor            | Adapter sadece application port/out implement eder ve **application.dto** üretir; API DTO ve api mapper infrastructure'da olmamalı. |
| Domain                                                | domain/model, domain/service mevcut; hesaplama domain'de                                                               | ✅ İyi.                                                                                                                              |
| GitHub ile iletişim                                   | Metrics, GitHub'ın port/in ve application.dto kullanıyor                                                               | ✅ Doğru (metrics-github-module-communication.md ile uyumlu).                                                                        |


**Öneri:**  

- RepositoryMetricsServicePort / UserMetricsServicePort imzalarını Application DTO (Info) dönecek şekilde değiştir.  
- Adapter'lar sadece application.dto ve domain kullanacak şekilde güncellensin; API DTO dönüşümü api katmanında (controller + RepositoryMetricsApiMapper / UserMetricsApiMapper) yapılsın.  
- Controller'da tek tip akış: port/in çağrısı → Application DTO → ApiMapper → API DTO.

---

### 2.4 Shared Modülü


| Konu                    | Durum                                  | Öneri                                                                                        |
| ----------------------- | -------------------------------------- | -------------------------------------------------------------------------------------------- |
| shared.domain.OwnerType | Auth ve GitHub tarafından kullanılıyor | Koru; auth'daki infrastructure.entity.OwnerType kaldırılsın, tek kaynak shared.domain olsun. |
| shared.exception        | Tüm modüller kullanıyor                | ✅ Uygun.                                                                                     |
| shared.logging          | Cross-cutting                          | ✅ Uygun.                                                                                     |


---

## 3. Refactor Fazları (Öncelik Sırasıyla)

### Faz 1: Modüller Arası Sınırlar (Modular Monolith)

**Amaç:** GitHub'ın Auth infrastructure'a doğrudan bağımlılığını kaldırmak.

1. **GitHub – PullRequestServiceAdapter (ve varsa diğer adapter'lar)**
  - `UserRepository` ve `User` (auth.infrastructure) kullanımını kaldır.  
  - Repo erişim kontrolü için sadece `AuthServicePort.findByLoginOrThrow(login)` (ve gerekirse başka Auth application port'u) kullan; dönen `UserInfo` ile yetki kontrolü yap.  
  - Gerekirse Auth tarafında "user exists" veya "user id" dönen ince bir port eklenebilir; entity hiçbir zaman GitHub'a geçmemeli.
2. **Auth – OwnerType tek kaynak**
  - `auth.infrastructure.persistence.entity.OwnerType` kaldır.  
  - Auth persistence entity (User) ve tüm auth mapper'lar `shared.domain.OwnerType` kullansın.

**Çıktı:** GitHub modülü Auth'ın sadece application katmanına (port + DTO) bağımlı; modüler monolith kuralı sağlanır.

---

### Faz 2: GitHub – Port ve DTO Katmanları

**Amaç:** Application'ın api ve infrastructure'a ters bağımlılığını kaldırmak; port'lar sadece application/domain tipi kullansın.

1. **Outbound port'lar (CommitClientPort, LanguageClientPort, AuthClientPort, BranchClientAdapter dönüş tipi, vb.)**
  - CommitClientPort: `List<CommitDto>` yerine `List<CommitInfo>` (application.dto) veya GitHub HTTP'ye özel bir `CommitClientResult` (application.dto) dönsün.  
  - LanguageClientPort: API LanguageDto yerine application.dto (örn. LanguageInfo) veya http.dto kullanılsın.  
  - AuthClientPort: Auth **application** tarafında bir "AuthUserInfo" veya mevcut LoginResult/UserInfo benzeri DTO tanımlanıp, GitHub'ın Auth'tan beklediği veri bu DTO ile gelmeli; Auth api.dto (GitHubUserDto vb.) GitHub'da kullanılmamalı.
2. **Infrastructure mapper'lar (BranchMapper, CommitMapper, LanguageMapper, PullRequestMapper, SyncJobMapper)**
  - Entity → **application.dto** (BranchInfo, CommitInfo, PullRequestInfo, SyncStatusInfo vb.) map etsin.  
  - API DTO'ya (BranchDto, CommitDto, …) dönüşüm **sadece** api/mapper katmanında (BranchApiMapper, CommitApiMapper, …) yapılsın; controller port'tan application DTO alıp api mapper ile API DTO'ya çevirsin.
3. **Eski service port'lar (BranchServicePort, CommitServicePort, LanguageServicePort, PullRequestServicePort, SyncJobServicePort, RepoServicePort vb.)**
  - Zaten port/in (GetBranchesPort, InitBranchesPort, …) ve use case'ler kullanılıyorsa, eski port'ları ve doğrudan kullanımlarını kaldır.  
  - Bir yerde hâlâ BranchServicePort.getBranchesByFullName → BranchDto kullanılıyorsa, o noktayı GetBranchesPort + BranchApiMapper ile değiştirip sonra eski port'u sil.
4. **SyncJobServicePort ve domain enum'lar**
  - SyncJobServicePort imzası SyncJobEntity, SyncJobStatus, SyncJobType (infrastructure) kullanıyorsa, bunları domain veya application.dto ile değiştir (örn. SyncStatusInfo, domain.enumeration.SyncJobStatus vb.).  
  - DataLevel: Persistence entity kendi enum'unu kullanabilir (JPA için); adapter içinde domain/enumeration.DataLevel'a map edilir. API ve application DTO'lar domain DataLevel kullanır.

**Çıktı:** GitHub application katmanı api ve infrastructure'dan bağımsız; tüm veri akışı application.dto ve domain üzerinden.

**Faz 2 ilerleme (yapıldı):**

- CommitClientPort: `List<CommitDto>` → `List<CommitInfo>`; adapter GitHubCommitResponse → CommitInfo map ediyor.
- LanguageClientPort: `LanguageDto` → `LanguageInfo`.
- AuthClientPort / GitHubServicePort: Auth api DTO'ları kaldırıldı; `GitHubOAuthResult`, `GitHubUserFromApi` (GitHub application.dto) kullanılıyor; Auth GitHubOAuthAdapter bu DTO'ları Auth API DTO'larına map ediyor.
- CommitMapper: entity ↔ CommitInfo; GitHub HTTP yanıtı için `GitHubCommitResponse` (infrastructure) + `fromGitHubResponse` → CommitInfo.

---

### Faz 3: Metrics – Port ve DTO Katmanları

**Amaç:** Metrics application port'larının API DTO döndürmesini kaldırmak; adapter'ların api katmanına bağımlılığını kaldırmak.

1. **RepositoryMetricsServicePort / UserMetricsServicePort**
  - Tüm metotlar **application.dto** (RepositoryMetricsInfo, RepositoryOverviewInfo, UserMetricsInfo, PrMergeRateInfo, vb.) dönecek şekilde değiştir.  
  - API DTO tipleri (RepositoryMetricsDto, RepositoryOverviewDto, …) bu port imzalarında yer almasın.
2. **RepositoryMetricsServiceAdapter / UserMetricsServiceAdapter**
  - Port'u application DTO (Info) dönecek şekilde implement et.  
  - **api.response** ve **api.mapper** import'larını kaldır; adapter sadece application.dto ve domain kullanarak sonuç üretsin.  
  - API DTO'ya dönüşüm: controller, port'tan gelen Info'yu **RepositoryMetricsApiMapper / UserMetricsApiMapper** ile API DTO'ya çevirip dönsün.
3. **Controller**
  - Tüm endpoint'ler aynı modeli kullansın: port/in (veya RepositoryMetricsServicePort/UserMetricsServicePort artık application DTO dönüyorsa onlar) çağrısı → Application DTO → ApiMapper → API DTO → ResponseEntity.

**Çıktı:** Metrics application katmanı api paketine bağımlı değil; infrastructure adapter'lar api kullanmıyor.

**Faz 3 ilerleme (yapıldı):**

- `RepositoryMetricsInfo` ve `UserMetricsInfo` (application.dto) eklendi.
- `RepositoryMetricsServicePort` / `UserMetricsServicePort`: Tüm metotlar artık application DTO (Info) dönüyor.
- `RepositoryMetricsServiceAdapter` / `UserMetricsServiceAdapter`: API response ve ApiMapper import'ları kaldırıldı; port'lar Info döndürüyor.
- Controller: `getRepositoryMetrics` ve `getUserMetrics` port'tan Info alıp `RepositoryMetricsApiMapper.toRepositoryMetricsDto(info)` / `UserMetricsApiMapper.toUserMetricsDto(info)` ile API DTO'ya çeviriyor.

---

### Faz 4: Auth – Port Yapısı ve Tutarlılık

**Amaç:** Auth modülünü standart port/in ve port/out yapısına getirmek.

1. **application/port**
  - Mevcut port'ları **port/in** ve **port/out** altında netleştir (zaten büyük ölçüde out altında; inbound AuthServicePort/LoginUseCase/GetCurrentUserUseCase port/in sayılır).  
  - Port/in: LoginUseCase, GetCurrentUserUseCase ile eşleşen arayüzler (örn. LoginPort, GetCurrentUserPort) application/port/in altında; implementasyon use case'lerde.
2. **Auth → API DTO**
  - Auth application port'ları zaten application DTO (LoginCommand, LoginResult, UserInfo) kullanıyorsa değişiklik yok.  
  - Controller, application DTO'yu api mapper ile API DTO'ya çevirsin (mevcut yapı buna yakınsa sadece dokümante et).

**Çıktı:** Auth modülü de standart paket ve bağımlılık kurallarına uyar.

---

### Faz 5: Domain Enum ve Entity Tutarlılığı (GitHub)

**Amaç:** Domain tek kaynak; infrastructure sadece JPA/persistence ihtiyacı için kendi enum/entity'sini kullanıp domain'e map etsin.

1. **DataLevel, SyncJobStatus, SyncJobType**
  - domain.enumeration'da tek tanım olsun.  
  - infrastructure.persistence.entity'deki enum'lar kaldırılabilir veya JPA @Enumerated için thin wrapper kalabilir; adapter her zaman domain enum'una map etsin.  
  - API ve application DTO'lar domain enum kullanır.
2. **İş kuralları**
  - DataLevel hesaplama, sync durumu belirleme vb. mümkün olduğunca domain service veya domain entity'de olsun; adapter sadece veri çekip domain'e parametre verir, sonucu persistence'a yazar.

**Çıktı:** GitHub domain net; infrastructure sadece teknik detay (JPA, HTTP).

---

## 4. Özet Tablo – Yapılacaklar (tamamlandı)

| #   | Görev                                                                                                | Modül   | Faz | Durum |
| --- | ---------------------------------------------------------------------------------------------------- | ------- | --- | ----- |
| 1   | GitHub adapter'larında User/UserRepository (Auth) kullanımını kaldır; sadece AuthServicePort kullan  | GitHub  | 1   | ✅    |
| 2   | auth.infrastructure.persistence.entity.OwnerType kaldır; shared.domain.OwnerType tek kaynak          | Auth    | 1   | ✅    |
| 3   | GitHub outbound port'ları application DTO (veya http.dto) kullanacak şekilde güncelle                | GitHub  | 2   | ✅    |
| 4   | GitHub infrastructure mapper'lar entity → application.dto; api mapper application.dto → api.response | GitHub  | 2   | ✅    |
| 5   | Eski GitHub service port'ları (BranchServicePort vb.) kaldır veya application DTO'ya geçir           | GitHub  | 2   | ✅    |
| 6   | SyncJobServicePort ve ilgili tipleri domain/application.dto ile değiştir                             | GitHub  | 2   | ✅    |
| 7   | RepositoryMetricsServicePort / UserMetricsServicePort Application DTO dönsün                         | Metrics | 3   | ✅    |
| 8   | Metrics adapter'lar api.response/api.mapper kullanmasın; controller'da ApiMapper kullan              | Metrics | 3   | ✅    |
| 9   | Auth port/in ve port/out yapısını standartlaştır                                                     | Auth    | 4   | ✅    |
| 10  | GitHub domain enum tek kaynak; infrastructure domain'e map etsin                                     | GitHub  | 5   | ✅    |


---

## 5. Referanslar

- **HEXAGONAL_ARCHITECTURE_REFACTOR_PLAN.md** – Detaylı use case ve port listesi, mevcut ihlaller.
- **metrics-github-module-communication.md** – Metrics ↔ GitHub iletişimi (port ve DTO kuralları).
- **metrics-module-hexagonal-refactor-plan.md** – Metrics modülü hexagonal refactor fazları.

Bu plan tamamlandığında tüm modüller aynı standart yapıda olacak; modüler monolith sınırları, hexagonal katman kuralları ve SOLID ilkeleri korunacaktır.