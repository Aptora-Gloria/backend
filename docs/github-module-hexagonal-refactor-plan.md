# GitHub Modülü – Hexagonal Refactor Planı

Bu doküman, GitHub modülünü Auth modülündeki hexagonal yapı ile uyumlu hale getirmek için adım adım planı içerir.

---

## 0. Mimari İlkeler (Zorunlu Kurallar)

Refactor’un tüm katmanları bu dört ilkeye uyar.

### 0.1 API Katmanı – Sadece Al / Ver

- **Sorumluluk:** Sadece isteği almak ve cevabı vermek.
- **Yapması gereken:** HTTP isteğini alır → Application katmanına uygun şekilde iletir (Port/In veya Use Case) → Gelen sonucu HTTP cevabına dönüştürüp döner.
- **Yapmaması gereken:** Validasyon, iş kuralları, orkestrasyon, domain/application DTO’lara derin bağımlılık. API DTO ↔ Application DTO dönüşümü (mapper) bu katmanda olabilir; mantık application’da kalır.

### 0.2 Application Katmanı – Validasyon ve Orkestrasyon

- **Sorumluluk:** Validasyon, orkestrasyon, domain’e yönlendirme.
- **Yapması gereken:**
  - Gelen isteği (application DTO/command) **validate** eder.
  - İş akışını **orkestre** eder: gerekli Port/Out’ları çağırır, domain service’lere delegasyon yapar, sonucu toplar.
  - Domain’e **sadece delegasyon**; iş kuralları domain’de koşar.
- **Port ayrımı:** Application (ve genel olarak modül) içinde port’lar **in** ve **out** olarak ayrılır (aşağıda).

### 0.3 Port’lar: In (Giriş) ve Out (Çıkış)

- **Port/In (inbound):** Dış dünyanın (API, CLI vb.) uygulamaya girmek için kullandığı arayüz. Controller, bir **Port/In** implementasyonunu (ör. Use Case) çağırır.
  - Örnek: `LoginPort`, `GetCurrentUserPort`, `QueryRepositoriesPort`, `GetPullRequestsPort`.
  - Konum: `application/port/in/`.
- **Port/Out (outbound):** Uygulamanın dış sistemlere (veritabanı, GitHub API, JWT vb.) erişmek için kullandığı arayüz. Use case veya domain’e veri taşıyan orkestrasyon bu port’ları çağırır; implementasyon infrastructure’da (adapter) olur.
  - Örnek: `UserPersistencePort`, `GitHubOAuthPort`, `JwtTokenPort`, `RepositoryPersistencePort`, `PullRequestQueryPort`.
  - Konum: `application/port/out/`.
- Tüm port’lar bu iki kategoride net ayrılır; “service port” veya “client” dağılımı port/in ve port/out isimlendirmesine göre yapılır.

### 0.4 Domain – Tüm İş Burada, Saf Java, Anotasyon Yok

- **Sorumluluk:** Tüm iş kuralları (business logic) domain’de koşar.
- **Kurallar:**
  - **Saf Java:** Domain paketi framework’den bağımsızdır; sadece Java (ve gerekirse domain’e ait basit bağımlılıklar).
  - **Anotasyon yok:** Domain sınıflarında `@Service`, `@Transactional`, `@Entity`, JPA, Spring vb. **hiçbir anotasyon veya framework bağımlılığı** olmaz.
  - **Girdi/çıktı:** Domain service’ler sadece method parametreleri ve return değerleriyle çalışır; doğrudan veritabanı, HTTP, dosya okuma/yazma yapmaz. Veri, application (use case) tarafından Port/Out üzerinden alınıp domain’e verilir; domain sonucu döndürür.
- **Konum:** `domain/model/`, `domain/service/`, `domain/vo/`, `domain/enumeration/` vb. Hepsi framework-free.

---

## 1. Referans: Auth Modülü Hexagonal Yapısı

### 1.1 Katman Akışı

```
API                 →  Controller (sadece al/ver, API DTO ↔ Application DTO mapper)
                         ↓ Port/In (örn. LoginPort, GetCurrentUserPort)
Application         →  Use Case (validasyon, orkestrasyon, domain’e delegasyon)
                         ↓
Domain              →  Domain Service (saf Java, anotasyon yok; tüm iş kuralları)
                         ↑ veri application tarafından Port/Out üzerinden gelir
Application         →  Port/Out (UserPersistencePort, GitHubOAuthPort, JwtTokenPort)
                         ↓
Infrastructure      →  Adapter (UserPersistenceAdapter, GitHubOAuthAdapter, JwtTokenAdapter)
```

### 1.2 Auth’taki Kurallar (İlkelere Göre)

| Konu | Auth’ta nasıl |
|------|----------------|
| **API** | Controller sadece isteği alır, Port/In (use case) çağırır, cevabı döner; mapper API ↔ Application DTO. |
| **Application** | Validasyon ve orkestrasyon use case’te; domain’e delegasyon. |
| **Port/In** | Controller’ın çağırdığı giriş arayüzü (use case’ler Port/In’i implement eder). |
| **Port/Out** | `application/port/out/`: UserPersistencePort, GitHubOAuthPort, JwtTokenPort. |
| **DTO** | Use case ve port’lar **Application DTO** kullanır; **API DTO** sadece api katmanında. |
| **Domain** | İş kuralları domain service’te; **saf Java, anotasyon yok**. Use case veriyi port/out’tan alıp domain’e verir, domain sonuç döndürür. |
| **Infrastructure** | HTTP adapter’lar `infrastructure/http/adapter/`, persistence `infrastructure/persistence/adapter/` altında. |

---

## 2. GitHub Modülü – Mevcut Durum Özeti

### 2.1 Tutarlı Olan Kısımlar

- **Repo akışı:** Controller → RepoApiPresenter → Use Case. Application DTO kullanımı ve port/out (RepositoryPersistencePort, UserContextPort, GitHubRepoQueryPort) doğru yerde.
- **application/port/out:** GitHubRepoQueryPort, RepositoryPersistencePort, UserContextPort zaten outbound port olarak tanımlı.

### 2.2 Tutarsızlıklar (Refactor Hedefleri)

| Sorun | Açıklama |
|-------|----------|
| **API sadece al/ver değil** | API katmanı sadece isteği alıp application’a iletmeli; validasyon veya iş mantığı API’de olmamalı. Gerekirse @Valid gibi minimal HTTP validasyonu kalabilir; asıl validasyon application’da. |
| **Port In/Out ayrımı yok** | Port’lar net **port/in** (controller’ın çağırdığı) ve **port/out** (application’ın dış sistemlere eriştiği) olarak ayrılmalı. Şu an “service port” ve “client” karışık. |
| **Controller → Service Port** | PullRequestController, BranchController vb. doğrudan **Service Port** kullanıyor; **Port/In** (use case arayüzü) yok. Controller sadece Port/In çağırmalı. |
| **Port = API DTO** | PullRequestServicePort, BranchServicePort vb. **API DTO** dönüyor. Application katmanı API’ye sızıyor; port’lar Application DTO kullanmalı. |
| **Client port konumu** | AuthClientPort, BranchClientPort vb. `application/client/` altında; hepsi **Port/Out** olarak `application/port/out/` altında olmalı. |
| **Presenter vs Controller** | Repo’da RepoApiPresenter var; ilke: API sadece al/ver. Mapping Controller’da, tek tip Controller + ApiMapper. |
| **Domain saf Java değil** | Domain paketinde Spring/JPA anotasyonu veya framework bağımlılığı varsa kaldırılmalı; tüm iş kuralları saf Java domain’de koşmalı. |
| **HTTP adapter konumu** | BranchClientAdapter vb. `infrastructure/http/` kökünde; hepsi `infrastructure/http/adapter/` altında olmalı. |

---

## 3. Refactor Fazları

### Faz 1: Port ve DTO Ayrımı (Application vs API)

**Amaç:** Application katmanının API DTO’ya bağımlılığını kaldırmak; port’lar Application DTO kullansın.

1. **Application DTO’larını tanımla** (sadece application ve domain bunları kullansın):
   - `application/dto/`: PullRequestInfo, ReviewInfo, ReviewCommentInfo, SyncStatusInfo, BranchInfo, CommitInfo, LanguageInfo vb.
2. **Port/Out ve adapter’ları güncelle:**  
   Persistence/HTTP port’ları (ve implementasyonları) Application DTO kullansın. Entity → Application DTO mapping adapter içinde; API DTO **sadece api katmanında**.
3. **Port/In tasarla:**  
   Controller’ın çağıracağı arayüzler `application/port/in/` altında tanımlanacak (Faz 3’te use case’ler bunları implement edecek). Şimdilik mevcut “service port” imzalarını Application DTO’ya çevir; Faz 3’te bunlar Port/In olarak taşınacak.
4. **API katmanı – sadece al/ver:**  
   Controller: isteği alır → Port/In (veya geçiş sürecinde use case) çağırır → gelen Application DTO’yu ApiMapper ile API DTO’ya çevirip döner. Validasyon **controller’da değil**, application (use case) içinde yapılacak.

**Çıktı:** Port’lar Application DTO kullanır; API DTO sadece api katmanında. API katmanı sadece al/ver ve mapping ile sınırlı.

---

### Faz 2: Port’ları In / Out Olarak Ayrıştırma

**Amaç:** Tüm port’ları **Port/In** (giriş) ve **Port/Out** (çıkış) olarak net ayrıma kavuşturmak.

1. **Port/Out (outbound) birleştir:**
   - `application/client/*Port` (AuthClientPort, BranchClientPort, CommitClientPort, LanguageClientPort, PullRequestClientPort) → `application/port/out/` taşı.
   - Zaten `application/port/out/` altında olanlar (GitHubRepoQueryPort, RepositoryPersistencePort, UserContextPort) kalır.
   - Tüm dış sistem erişimi (DB, HTTP, JWT vb.) tek dizinde: `application/port/out/`.
2. **Port/In (inbound) tanımla:**
   - Controller’ın çağıracağı arayüzler `application/port/in/` altında tanımlanır.
   - Örnek: `QueryRepositoriesPort`, `GetPullRequestsPort`, `GetBranchesPort`, `InitiatePullRequestSyncPort` vb.
   - Use case sınıfları bu Port/In arayüzlerini implement eder (Faz 3).
3. **Referansları güncelle:**  
   Port/Out kullanan use case ve adapter import’ları `application.port.out`; controller’ın bağımlılığı Port/In olacak şekilde güncelle.

**Çıktı:** Port’lar net **port/in** (giriş – controller çağırır) ve **port/out** (çıkış – application dış sistemlere erişir) ayrımına uygun.

---

### Faz 3: Use Case’ler, Port/In ve Controller Bağlantısı

**Amaç:** Controller sadece Port/In çağırsın; Application (use case) validasyon + orkestrasyon yapsın, domain’e delegasyon; domain saf Java.

1. **Port/In arayüzlerini tanımla** (`application/port/in/`):
   - Örn. QueryRepositoriesPort, GetPullRequestsPort, GetPullRequestPort, GetBranchesPort, GetCommitsPort, GetLanguagesPort, InitiatePullRequestSyncPort, GetSyncStatusPort, GetRepoSyncStatusPort vb.
   - Metod imzaları Application DTO (command/query ve result) kullanır.
2. **Use case sınıflarını ekle/güncelle; Port/In implement etsinler:**
   - Her use case ilgili Port/In’i implement eder (örn. GetPullRequestsUseCase implements GetPullRequestsPort).
   - **Validasyon:** Use case girişi (application DTO) validate eder; geçersizse domain’e hiç gitmeden uygun exception.
   - **Orkestrasyon:** Port/Out’ları çağırır (UserContextPort, RepositoryPersistencePort, SyncJobServicePort, PullRequestClientPort vb.), gerekli veriyi toplar.
   - **Domain’e delegasyon:** İş kuralı gereken yerlerde domain service’e parametre geçirir; domain **saf Java**, anotasyon yok, sadece sonuç döndürür. Veri use case tarafından Port/Out’tan alınıp domain’e verilir; domain I/O yapmaz.
3. **Controller’ları güncelle:**
   - Dependency: **Port/In** (use case inject edilir ama tip Port/In olur).
   - Sadece al/ver: isteği al → Port/In çağır → Application DTO’yu ApiMapper ile API DTO’ya çevir → dön.

**Çıktı:** API sadece al/ver; Application validasyon + orkestrasyon + Port/Out + domain delegasyon; Domain saf Java, anotasyon yok.

---

### Faz 3b: Domain Paketi – Saf Java, Anotasyon Yok

**Amaç:** Tüm iş kurallarının domain’de koşması; domain’in framework’den tamamen bağımsız olması.

1. **Domain paketini tara:**  
   `domain/` altında `@Service`, `@Entity`, `@Transactional`, JPA, Spring import’u vb. varsa **kaldır**.
2. **Domain sadece girdi/çıktı ile çalışsın:**  
   Domain service’ler dış sistemlere (DB, HTTP) doğrudan erişmesin. Veri use case tarafından Port/Out’tan alınıp domain’e parametre olarak verilir; domain sadece hesaplama / iş kuralı uygulayıp sonuç döndürür.
3. **Instantiation:**  
   Domain sınıfları infrastructure (config/factory) veya application tarafından oluşturulup use case’e inject edilir; domain paketinde Spring anotasyonu olmaz.

**Çıktı:** Domain paketi saf Java; tüm iş kuralları burada, anotasyon yok.

---

### Faz 4: Repo – Presenter Kaldırma (Opsiyonel ama Önerilen)

**Amaç:** Repo’da da API sadece al/ver; Controller Port/In (use case) çağırır, mapping Controller’da.

1. **RepoController:**  
   RepoApiPresenter yerine doğrudan Port/In (QueryRepositoriesPort, InitializeUserRepositoriesPort vb.) inject et (use case implement eder).
2. **Mapping:**  
   Application DTO → API DTO dönüşümü Controller içinde ApiMapper ile.
3. **RepoApiPresenter:**  
   Kullanım kalktıktan sonra kaldır.

**Çıktı:** Repo da aynı ilkelerle: API al/ver, Application validasyon + orkestrasyon, Port/In–Port/Out ayrımı.

---

### Faz 5: Infrastructure Düzenlemesi

**Amaç:** HTTP adapter’ların konumunu Auth ile uyumlu hale getirmek.

1. **HTTP adapter konumu:**  
   `infrastructure/http/BranchClientAdapter.java` vb. → `infrastructure/http/adapter/BranchClientAdapter.java` (Auth’taki `http/adapter/` gibi).  
   Zaten `GitHubRepoQueryAdapter` `http/adapter/` altında; diğer client adapter’ları da aynı alt dizine taşı.
2. **Package ve import’lar:**  
   Taşıma sonrası package ve tüm referansları güncelle.

**Çıktı:** Tüm HTTP adapter’lar `infrastructure/http/adapter/` altında.

---

## 4. Özet Kontrol Listesi (Hexagonal Uyum)

Refactor sonrası aşağıdakiler sağlanmış olmalı:

**API katmanı – sadece al/ver**
- [ ] API katmanı sadece isteği alır, uygun şekilde application’a (Port/In) iletir, cevabı döner.
- [ ] Validasyon ve iş mantığı API’de yok; API DTO ↔ Application DTO dönüşümü (mapper) bu katmanda olabilir.

**Application katmanı – validasyon ve orkestrasyon**
- [ ] Validasyon application (use case) içinde yapılıyor.
- [ ] Orkestrasyon application’da; domain’e delegasyon var; iş kuralları domain’de koşuyor.
- [ ] Port’lar net **Port/In** (`application/port/in/`) ve **Port/Out** (`application/port/out/`) olarak ayrılmış.
- [ ] Controller yalnızca **Port/In** (use case implementasyonu) çağırıyor.
- [ ] Use case ve port’lar **Application DTO** kullanıyor; **API DTO** sadece **api** paketinde.

**Domain – tüm iş burada, saf Java, anotasyon yok**
- [ ] Tüm iş kuralları (business logic) domain’de; domain paketi **saf Java**, **hiçbir anotasyon yok** (@Service, @Entity, JPA, Spring vb.).
- [ ] Domain sınıfları sadece parametre/return ile çalışıyor; doğrudan I/O (DB, HTTP) yok; veri application (Port/Out) üzerinden geliyor.

**Infrastructure**
- [ ] Tüm outbound port’lar **application/port/out/** altında; implementasyonlar infrastructure’da (adapter).
- [ ] HTTP adapter’lar **infrastructure/http/adapter/** altında.

---

## 5. Başlama Sırası Önerisi

1. **Faz 1:** Port ve DTO ayrımı (Application DTO, port’lar API DTO kullanmasın); API katmanı sadece al/ver + mapper.
2. **Faz 2:** Port’ları **Port/In** ve **Port/Out** olarak net ayrıştırma; client port’ları port/out’a taşıma.
3. **Faz 3:** Use case’ler (Port/In implement), validasyon + orkestrasyon application’da, domain’e delegasyon; controller sadece Port/In çağırsın.
4. **Faz 3b:** Domain paketini saf Java yap; anotasyon ve framework bağımlılığı kaldır; tüm iş kuralları domain’de.
5. **Faz 4** ve **Faz 5:** Repo presenter kaldırma, HTTP adapter’ları `http/adapter/` altına taşıma.

İlk adım: Faz 1 için tek bir bounded context (örn. Pull Request) seçip Application DTO + port imzaları + adapter güncellemesi; sonra aynı şablonu diğer alanlara yaymak. Tüm refactor boyunca dört ilke geçerli: **(1) API sadece al/ver, (2) Application validasyon + orkestrasyon + domain’e delegasyon, (3) Port’lar in/out ayrımı, (4) Domain saf Java, anotasyon yok.**

---

## 6. Task Listesi (Basit ve Çalışılabilir)

Aşağıdaki task’lar sırayla yapılabilir; her biri tek bir değişiklik kümesi.

### Faz 1 – Port ve DTO Ayrımı

- [x] **T1.1** Domain enum’lar: `DataLevel`, `SyncJobStatus`, `SyncJobType` domain’de oluşturuldu; API DTO ve mapper’da domain enum kullanılıyor; infrastructure entity’ler şimdilik kendi enum’larıyla kalmaya devam ediyor (adapter’da dönüşüm yapılıyor).
- [x] **T1.2** Application DTO – Pull Request: `PullRequestInfo`, `ChangeStatsInfo` (`application/dto/`), domain `DataLevel` kullanılıyor.
- [x] **T1.3** Application DTO – Review/Commit/Sync: `ReviewInfo`, `ReviewCommentInfo`, `SyncStatusInfo` (+ `JobStatusInfo`), domain `SyncJobStatus`/`SyncJobType` kullanılıyor.
- [x] **T1.4** Application DTO – Branch, Commit, Language: `BranchInfo`, `CommitInfo`, `LanguageInfo` oluşturuldu.
- [x] **T1.5** Port/In – Pull Request: `GetPullRequestsPort`, `GetPullRequestPort`, `GetPullRequestReviewsPort`, `GetPullRequestCommitsPort`, `InitiatePullRequestSyncPort`, `GetSyncStatusPort` (`application/port/in/`) Application DTO imzalarıyla tanımlandı.
- [x] **T1.6** PullRequestServiceAdapter: PullRequestQueryPort (Port/Out) implement ediyor; entity → Application DTO mapping PullRequestMapper/SyncJobMapper üzerinden; use case’ler Port/In implement edip bu port’u çağırıyor.
- [x] **T1.7** PullRequestController: Port/In çağırıyor; Application DTO → API DTO için `PullRequestApiMapper` kullanıyor; sadece al/ver.

### Faz 2 – Port In/Out Ayrımı

- [x] **T2.1** Client port’ları `application/client/` → `application/port/out/` taşındı (AuthClientPort, BranchClientPort, CommitClientPort, LanguageClientPort, PullRequestClientPort).
- [x] **T2.2** Port/In arayüzleri `application/port/in/` altında; controller Port/In (use case) inject ediyor.

### Faz 3 – Use Case ve Controller

- [x] **T3.1** Pull Request use case’leri: GetPullRequestsUseCase, GetPullRequestUseCase, … (Port/In implement); PullRequestQueryPort (out); validasyon use case’te.
- [x] **T3.2** Branch, Commit, Language use case’leri: BranchQueryPort, CommitQueryPort, LanguageQueryPort (out); InitBranchesPort/GetBranchesPort, InitCommitsPort/GetCommitsPort, InitLanguagesPort/GetLanguagesPort (in); use case’ler ve ApiMapper eklendi; controller’lar Port/In kullanıyor.
- [x] **T3.3** PullRequest, Branch, Commit, Language controller’lar sadece Port/In (use case) çağırıyor; API sadece al/ver.
- [x] **T3.4** RepoSyncStatus: GetRepoSyncStatusPort (in), RepoSyncStatusQueryPort (out), GetRepoSyncStatusUseCase, RepoSyncStatusApiMapper; RepositorySyncStatusController Port/In + ApiMapper kullanıyor; eski RepoSyncStatusServicePort kaldırıldı.

### Faz 3b – Domain Saf Java

- [x] **T3b.1** GitHub domain paketi zaten saf Java; `@Service`, `@Entity`, JPA, Spring yok; RepositoryDomainService, RepositoryFullName, enumeration’lar framework-free.

### Faz 4 – Repo Presenter

- [x] **T4.1** RepoController: RepoApiPresenter kaldırıldı; Port/In (QueryRepositoriesPort, InitializeUserRepositoriesPort) inject ediliyor; mapping Controller’da ApplicationToApiMapper ile.
- [x] **T4.2** RepoApiPresenter sınıfı kaldırıldı.

### Faz 5 – Infrastructure

- [x] **T5.1** HTTP adapter’ları `infrastructure/http/` → `infrastructure/http/adapter/` taşındı (AuthClientAdapter, BranchClientAdapter, CommitClientAdapter, LanguageClientAdapter, PullRequestClientAdapter); package `infrastructure.http.adapter` olarak güncellendi.
