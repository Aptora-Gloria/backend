# Metrics Modülü – Hexagonal Refactor Planı

Bu doküman, Metrics modülünü hexagonal mimari ile uyumlu hale getirmek ve **metrik hesaplamalarının domain’de, temiz ve okunur** olmasını sağlamak için adım adım planı içerir.

---

## Kullanıcı Özeti (Refactor Yönü)

1. **API katmanı** sadece isteği al/ver yapmakla sorumlu; isteği alır, uygun şekilde application katmanına iletir.
2. **Application katmanı** validasyon yapacak, orkestrasyon ve işlemler için domain'e yönlenecek; aynı zamanda app katmanında ve genel olarak port'lar **in** ve **out** olarak ayrılacak.
3. **Bütün iş (business)** domain'de koşacak; **saf Java** olacak, **anotasyon olmayacak**.

Bu üç madde tüm faz ve task'larda zorunludur.

---

## 0. Mimari İlkeler (Zorunlu Kurallar)

Refactor’un tüm katmanları bu dört ilkeye uyar.

### 0.1 API Katmanı – Sadece Al / Ver

- **Sorumluluk:** Sadece isteği almak ve cevabı vermek.
- **Yapması gereken:** HTTP isteğini alır → Application katmanına (Port/In) uygun şekilde iletir → Gelen Application DTO’yu ApiMapper ile API DTO’ya çevirir → HTTP cevabı olarak döner.
- **Yapmaması gereken:** Validasyon, iş kuralları, orkestrasyon, metrik hesaplama. API DTO ↔ Application DTO dönüşümü (ApiMapper) bu katmanda olabilir; mantık application ve domain’de kalır.

### 0.2 Application Katmanı – Validasyon ve Orkestrasyon

- **Sorumluluk:** Validasyon, orkestrasyon, domain’e yönlendirme.
- **Yapması gereken:**
  - Gelen isteği (login, repoFullName vb.) **validate** eder.
  - İş akışını **orkestre** eder: Port/Out ile ham veriyi alır, domain service’e parametre olarak verir, sonucu Application DTO’ya çevirip döner.
  - **Domain’e sadece delegasyon;** tüm metrik hesaplama ve iş kuralları domain’de koşar.
- **Port ayrımı:** Port’lar **Port/In** (giriş – controller çağırır) ve **Port/Out** (çıkış – veri/entity erişimi) olarak ayrılır.

### 0.3 Port’lar: In (Giriş) ve Out (Çıkış)

- **Port/In (inbound):** Controller’ın çağırdığı arayüz. Use case bu arayüzü implement eder.
  - Örnek: `GetRepositoryMetricsPort`, `GetOverviewPort`, `GetUserMetricsPort`, `GetPrMergeRatePort`.
  - Konum: `application/port/in/`.
- **Port/Out (outbound):** Uygulamanın metrik hesaplamak için ihtiyaç duyduğu ham veriyi sağlayan arayüz. Implementasyon infrastructure (adapter) tarafında.
  - Örnek: `RepositoryMetricsDataPort`, `UserMetricsDataPort` (veya mevcut GitHub/Auth port’ları ile veri çekme).
  - Konum: `application/port/out/`.

### 0.4 Domain – Tüm İş Burada, Saf Java, Anotasyon Yok

- **Sorumluluk:** Tüm metrik hesaplama ve iş kuralları (business logic) domain’de koşar. **Okunabilirlik ve test edilebilirlik çok önemli.**
- **Kurallar:**
  - **Saf Java:** Domain paketi framework’den bağımsızdır; sadece Java (ve gerekirse `java.math`, `java.time` vb.).
  - **Anotasyon yok:** `@Service`, `@Transactional`, `@Entity`, JPA, Spring vb. **hiçbir anotasyon veya framework bağımlılığı** olmaz.
  - **Girdi/çıktı:** Domain sınıfları sadece parametre ve return değerleriyle çalışır; doğrudan veritabanı/HTTP yok. Veri application (use case) tarafından Port/Out’tan alınıp domain’e **parametre** olarak verilir; domain sadece hesaplama yapıp sonuç döndürür.
- **Konum:** `domain/service/`, `domain/model/` (veya `domain/dto/` – sadece domain’e özgü girdi/çıktı tipleri, anotasyonsuz).

---

## 1. Referans: GitHub Modülü ve Auth Akışı

### 1.1 Hedef Akış (Metrics İçin)

```
API                 →  Controller (sadece al/ver; isteği al → Port/In çağır → ApiMapper ile API DTO dön)
                         ↓ Port/In (örn. GetRepositoryMetricsPort, GetUserMetricsPort)
Application         →  Use Case (validasyon, orkestrasyon, Port/Out ile veri al, domain’e ver, sonucu Application DTO yap)
                         ↓
Domain              →  Domain Service (saf Java; tüm metrik formülleri, yüzdeler, ortalamalar, skorlar burada)
                         ↑ ham veri application tarafından Port/Out’tan alınıp domain’e parametre olarak gelir
Application         →  Port/Out (RepositoryMetricsDataPort, UserMetricsDataPort – ham veri sağlar)
                         ↓
Infrastructure     →  Adapter (JPA/entity kullanır, entity’leri domain’in anlayacağı “ham veri” yapısına çevirir)
```

**Uygulanan akış (PR metrikleri):** Controller → GetPullRequestMetricsPort (Use Case) → RepositoryMetricsDataPort.getPrMetricsInput (ham veri) → PrMetricsCalculator.calculate (Domain) → Use Case sonucu RepositoryPrMetricsInfo’ya map eder. Port/Out = RepositoryMetricsDataPort (sadece PrMetricsInput döner); Adapter sadece DB’den okuyup PrRaw listesine çevirir.

### 1.2 Kurallar Özeti

| Konu | Nasıl olacak |
|------|----------------|
| **API** | Controller sadece isteği alır, Port/In çağırır, Application DTO’yu ApiMapper ile API DTO’ya çevirip döner. |
| **Application** | Validasyon ve orkestrasyon use case’te; Port/Out ile veri alınır, domain’e parametre verilir, sonuç Application DTO’ya map edilir. |
| **Port/In** | Controller’ın çağırdığı giriş arayüzü (use case’ler implement eder); Application DTO döner. |
| **Port/Out** | Ham veri sağlayan arayüzler (`application/port/out/`). |
| **DTO** | Use case ve Port/In **Application DTO** kullanır; **API DTO** sadece api katmanında (ApiMapper ile dönüşüm). |
| **Domain** | Tüm hesaplama (yüzde, ortalama, skor, trend) **saf Java**, anotasyon yok; sadece parametre alır, sonuç döner. |
| **Infrastructure** | Adapter Port/Out’u implement eder; entity’den “domain input” (ham veri) üretir; hesaplama yapmaz. |

---

## 2. Metrics Modülü – Mevcut Durum Özeti

### 2.1 Mevcut Yapı

- **API:** `RepositoryMetricsController`, `MetricsController` – doğrudan `RepositoryMetricsServicePort` / `UserMetricsServicePort` çağırıyor; port’lar **API DTO** dönüyor.
- **Application:** Sadece port arayüzleri var; use case yok. Port’lar aslında “service port” ve API DTO kullanıyor.
- **Infrastructure:** `RepositoryMetricsServiceAdapter`, `UserMetricsServiceAdapter` hem **veri erişimi** hem de **tüm metrik hesaplama** (BigDecimal, divide, multiply, stream, Comparator) içeriyor – bu iş domain’de olmalı.

### 2.2 Tutarsızlıklar (Refactor Hedefleri)

| Sorun | Açıklama |
|-------|----------|
| **API sadece al/ver değil** | Controller doğrudan “service port” kullanıyor ve port API DTO dönüyor; API katmanı sadece al/ver + ApiMapper olmalı, port’lar Application DTO kullanmalı. |
| **Port In/Out ayrımı yok** | `RepositoryMetricsServicePort` / `UserMetricsServicePort` tek tip; bunlar **Port/In** olmalı (controller çağırır). Ham veri için **Port/Out** (RepositoryMetricsDataPort, UserMetricsDataPort) ayrı tanımlanmalı. |
| **Port = API DTO** | Service port’lar `RepositoryMetricsDto`, `RepositoryOverviewDto` vb. API DTO dönüyor; Application katmanı **Application DTO** kullanmalı, API DTO sadece api paketinde. |
| **İş mantığı adapter’da** | Tüm metrik hesaplama (merge rate, ortalama süre, yüzde dağılımı, health score, activity trend vb.) adapter içinde; **domain’de**, saf Java ve okunur şekilde olmalı. |
| **Domain yok** | Metrics modülünde `domain` paketi yok; hesaplama mantığı domain’e taşınacak, saf Java, anotasyon yok. |

---

## 3. Refactor Fazları

### Faz 1: Application DTO ve Port/In – API DTO’dan Ayrılma

**Amaç:** Application katmanının API DTO’ya bağımlılığını kaldırmak; controller sadece al/ver + ApiMapper kullansın.

1. **Application DTO’larını tanımla** (`application/dto/` veya `application/dto/metrics/`):
   - Repository: `RepositoryMetricsInfo`, `RepositoryOverviewInfo`, `RepositoryPrMetricsInfo`, `RepositoryCommitMetricsInfo`, `RepositoryReviewMetricsInfo`, `RepositoryBranchMetricsInfo`, `RepositoryLanguageMetricsInfo`, `RepositoryActivityTrendInfo`, `RepositoryCodeQualityInfo`, `RepositoryContributorMetricsInfo`, `RepositoryHealthScoreInfo`.
   - User: `UserMetricsInfo`, `PrMergeRateInfo`, `PrSizeInfo`, `ReviewStatsInfo`, `CommitTimeAnalysisInfo`, `LanguageDiversityInfo`, `ActivityTrendInfo`.
   - Bu DTO’lar API DTO ile aynı alanları taşır ama application/domain tarafında kullanılır; API DTO sadece api katmanında kalır.
2. **Port/In arayüzlerini tanımla** (`application/port/in/`):
   - Repository: `GetRepositoryMetricsPort`, `GetOverviewPort`, `GetPullRequestMetricsPort`, `GetCommitMetricsPort`, `GetReviewMetricsPort`, `GetBranchMetricsPort`, `GetLanguageMetricsPort`, `GetActivityTrendPort`, `GetCodeQualityPort`, `GetContributorMetricsPort`, `GetHealthScorePort` – hepsi Application DTO döner (veya gruplu tek port: `GetRepositoryMetricsPort` tek metot ile tüm metrikleri dönebilir; alt metrikler için ayrı port’lar da olabilir).
   - User: `GetUserMetricsPort`, `GetPrMergeRatePort`, `GetPrSizePort`, `GetReviewStatsPort`, `GetCommitTimeAnalysisPort`, `GetLanguageDiversityPort`, `GetActivityTrendPort` (user) – Application DTO döner.
3. **ApiMapper’lar:** API katmanında Application DTO → API DTO dönüşümü için `RepositoryMetricsApiMapper`, `UserMetricsApiMapper` (veya tek `MetricsApiMapper`) ekle; controller sadece Port/In çağırır, gelen Application DTO’yu ApiMapper ile API DTO’ya çevirip döner.

**Çıktı:** Port/In Application DTO döner; API katmanı sadece al/ver + ApiMapper; API DTO sadece api paketinde.

---

### Faz 2: Port/Out – Ham Veri Sağlama

**Amaç:** Metrik hesaplamak için gerekli ham veriyi sağlayan Port/Out’ları tanımlamak; adapter sadece veri çekip “domain input” üretsin, hesaplama yapmasın.

1. **Port/Out tanımla** (`application/port/out/`):
   - `RepositoryMetricsDataPort`: Repository için ham veri (overview verileri, PR listesi, commit listesi, review listesi, branch listesi, language listesi vb.) – metotlar domain’in ihtiyaç duyduğu “raw” yapıları döner (ör. `OverviewRaw`, `PrListRaw`, `CommitListRaw` vb. veya tek bir `RepositoryMetricsRaw` aggregate).
   - `UserMetricsDataPort`: User için ham veri (kullanıcının repo’ları, PR’ları, review’ları, commit’leri, dil dağılımı vb.).
   - Bu yapılar anotasyonsuz, saf Java record/class; domain’e parametre olarak verilecek.
2. **Adapter’lar:** Mevcut `RepositoryMetricsServiceAdapter` / `UserMetricsServiceAdapter` yerine:
   - `RepositoryMetricsDataAdapter` (RepositoryMetricsDataPort implement) – sadece JPA’dan veri çeker, entity’leri “raw” domain input’a map eder; **hesaplama yok**.
   - Use case (Faz 3) Port/Out’u çağırır, raw veriyi domain’e verir, domain sonucu döner, use case Application DTO’ya map eder.

**Çıktı:** Port/Out ile ham veri ayrıldı; hesaplama adapter’da kalmayacak, domain’e taşınacak.

---

### Faz 3: Use Case’ler ve Domain’e Taşıma

**Amaç:** Use case’ler Port/In implement etsin; validasyon + orkestrasyon application’da, tüm hesaplama domain’de.

1. **Domain input/output tipleri (saf Java):**
   - Domain’in hesaplama yapacağı girdi tipleri: örn. `OverviewInput`, `PrMetricsInput`, `CommitMetricsInput`, `MergeRateInput` vb. – sadece veri taşır, anotasyon yok.
   - Domain’in döndüreceği çıktı tipleri: örn. `OverviewResult`, `PrMetricsResult`, `MergeRateResult` vb. – hesaplanmış değerler.
2. **Domain service’ler** (`domain/service/` veya `domain/calculator/`):
   - Örn. `OverviewMetricsCalculator`, `PrMetricsCalculator`, `CommitMetricsCalculator`, `ReviewMetricsCalculator`, `BranchMetricsCalculator`, `LanguageMetricsCalculator`, `ActivityTrendCalculator`, `CodeQualityCalculator`, `ContributorMetricsCalculator`, `HealthScoreCalculator`.
   - User: `PrMergeRateCalculator`, `PrSizeCalculator`, `ReviewStatsCalculator`, `CommitTimeAnalysisCalculator`, `LanguageDiversityCalculator`, `ActivityTrendCalculator` (user).
   - Her biri saf Java: parametre olarak raw input alır, return olarak result döner; BigDecimal, RoundingMode, stream kullanımı **sadece burada**.
3. **Use case’ler** (`application/usecase/`):
   - Örn. `GetRepositoryMetricsUseCase` implements `GetRepositoryMetricsPort`; `GetOverviewUseCase` implements `GetOverviewPort`; … `GetUserMetricsUseCase` implements `GetUserMetricsPort`; …
   - Use case: login/repoFullName validate → Port/Out (RepositoryMetricsDataPort / UserMetricsDataPort) ile raw veri al → domain calculator’a ver → domain sonucunu Application DTO’ya map et → dön.
4. **Controller:** Port/In (use case) inject eder; isteği alır → Port/In çağırır → ApiMapper(Application DTO → API DTO) → ResponseEntity ile dön.

**Çıktı:** Application validasyon + orkestrasyon; domain’de tüm hesaplama; API sadece al/ver.

---

### Faz 4: Domain – Saf Java, Okunabilir Hesaplama

**Amaç:** Tüm metrik formülleri domain’de, saf Java, anotasyon yok; okunabilir ve test edilebilir.

1. **Domain paketi yapısı:**
   - `domain/model/` veya `domain/dto/`: Girdi/çıktı tipleri (record veya POJO, anotasyonsuz).
   - `domain/service/` veya `domain/calculator/`: Hesaplama sınıfları; sadece `java.*`, `java.math.*`, `java.time.*` kullanılır.
2. **Kurallar:**
   - Domain sınıflarında `@Service`, `@Transactional`, `@Entity`, JPA, Spring **yok**.
   - Hesaplama mantığı (yüzde, ortalama, skor, trend) tek yerde, isimlendirilmiş metotlarla okunur olmalı.
3. **Adapter’dan taşıma:** Şu an adapter’da olan tüm `BigDecimal`, `divide`, `multiply`, `stream`, `Comparator` kullanımları domain calculator’lara taşınır; adapter sadece raw veri üretir ve Port/Out’u implement eder.

**Çıktı:** Domain saf Java; tüm iş kuralları ve metrik hesaplamaları burada; okunabilir ve test edilebilir.

---

### Faz 5: Eski Port ve Adapter Temizliği

**Amaç:** Eski “service port” ve “hesaplama yapan adapter” kaldırılıp yeni Port/In, Port/Out, use case ve domain adapter’lar ile değiştirilmesi.

1. **RepositoryMetricsServicePort / UserMetricsServicePort** kaldırılır; yerine Port/In (GetRepositoryMetricsPort vb.) kullanılır.
2. **RepositoryMetricsServiceAdapter** / **UserMetricsServiceAdapter**:
   - Ya tamamen kaldırılıp yerine `RepositoryMetricsDataAdapter` (sadece Port/Out – ham veri) + use case’ler (Port/In) + domain calculator’lar gelir.
   - Ya da mevcut adapter ikiye ayrılır: (1) Data adapter (Port/Out), (2) Use case Port/In implement eder ve data adapter + domain’i kullanır; eski “service adapter” kalkar.
3. **Controller** bağımlılıkları Port/In (use case) olacak şekilde güncellenir; ApiMapper ile Application DTO → API DTO.

**Çıktı:** Tek bir tutarlı akış: API (al/ver) → Port/In (use case) → Port/Out (data) + Domain (hesaplama).

---

## 4. Özet Kontrol Listesi (Hexagonal Uyum)

Refactor sonrası aşağıdakiler sağlanmış olmalı:

**API katmanı – sadece al/ver**
- [ ] Controller sadece isteği alır, Port/In çağırır, Application DTO’yu ApiMapper ile API DTO’ya çevirip cevabı döner.
- [ ] Validasyon ve metrik hesaplama API’de yok.

**Application katmanı – validasyon ve orkestrasyon**
- [ ] Validasyon use case içinde (login, repoFullName vb.).
- [ ] Orkestrasyon use case’te: Port/Out ile ham veri alınır, domain’e verilir, sonuç Application DTO’ya map edilir.
- [ ] Port’lar net **Port/In** (`application/port/in/`) ve **Port/Out** (`application/port/out/`) ayrımına uygun.
- [ ] Use case ve Port/In **Application DTO** kullanır; **API DTO** sadece api paketinde.

**Domain – tüm iş burada, saf Java, anotasyon yok**
- [x] Domain paketi mevcut: `com.optimetric.metrics.domain` (model + service). PR metrikleri hesaplaması domain’de (PrMetricsCalculator).
- [ ] Tüm metrik hesaplama (yüzde, ortalama, skor, trend) domain’de; **saf Java**, **anotasyon yok** (diğer metrikler taşınacak).
- [x] Domain sadece parametre/return ile çalışır; doğrudan I/O yok; veri adapter üzerinden PrRaw/PrMetricsInput olarak gelir.
- [x] Domain kodu okunabilir ve test edilebilir (PrMetricsCalculator).

**Infrastructure**
- [ ] Port/Out implementasyonları (RepositoryMetricsDataAdapter, UserMetricsDataAdapter) sadece veri çeker ve “domain input” üretir; hesaplama yapmaz.

---

## 5. Başlama Sırası Önerisi

1. **Faz 1:** Application DTO + Port/In (Application DTO döner) + ApiMapper; controller’ı Port/In + ApiMapper kullanacak şekilde güncelle; eski port’u bir süre adapter’da tutup implementasyonu yeni Port/In’e yönlendirebilirsin.
2. **Faz 2:** Port/Out (RepositoryMetricsDataPort, UserMetricsDataPort) ve “raw” domain input tiplerini tanımla; adapter’ı sadece veri çeken + raw üreten hale getir (hesaplama kısmını geçici olarak use case’te veya eski adapter’da tutulabilir).
3. **Faz 3:** Domain calculator’ları yaz; use case’ler Port/Out’tan veri alıp domain’e verip sonucu Application DTO’ya map etsin.
4. **Faz 4:** Tüm hesaplama mantığını adapter’dan domain’e taşı; domain’i saf Java ve okunur hale getir.
5. **Faz 5:** Eski service port ve “hesaplama yapan” adapter’ı kaldır; tek akış: API → Port/In (use case) → Port/Out (data) + Domain (hesaplama).

İlk adım: **Faz 1** ile tek bir metrik (örn. Overview veya PrMergeRate) seçip Application DTO + Port/In + ApiMapper + controller güncellemesi; sonra aynı şablonu diğer metriklerine yaymak. Tüm refactor boyunca dört ilke geçerli: **(1) API sadece al/ver, (2) Application validasyon + orkestrasyon + domain’e delegasyon, (3) Port’lar in/out ayrımı, (4) Domain saf Java, anotasyon yok, metrik hesaplama domain’de okunur şekilde.**

---

## 6. Task Listesi (Çalışılabilir)

### Faz 1 – Application DTO ve Port/In

- [x] **T1.1** Application DTO – Repository: `RepositoryOverviewInfo`, `RepositoryLanguageMetricsInfo`, `RepositoryPrMetricsInfo`, `RepositoryCommitMetricsInfo`, `RepositoryActivityTrendInfo`, `RepositoryCodeQualityInfo`, `RepositoryContributorMetricsInfo`, `RepositoryHealthScoreInfo`, `RepositoryReviewMetricsInfo`, `RepositoryBranchMetricsInfo` eklendi.
- [x] **T1.2** Application DTO – User: `PrMergeRateInfo`, `PrSizeInfo`, `ReviewStatsInfo`, `CommitTimeAnalysisInfo`, `LanguageDiversityInfo`, `ActivityTrendInfo` eklendi.
- [x] **T1.3** Port/In – Repository: `GetOverviewPort`, `GetLanguageMetricsPort`, `GetPullRequestMetricsPort`, `GetCommitMetricsPort`, `GetRepositoryActivityTrendPort`, `GetCodeQualityPort`, `GetContributorMetricsPort`, `GetHealthScorePort`, `GetReviewMetricsPort`, `GetBranchMetricsPort`; Port/Out `RepositoryMetricsQueryPort` tüm *Info metotları.
- [x] **T1.4** Port/In – User: `GetPrMergeRatePort`, `GetPrSizePort`, `GetReviewStatsPort`, `GetCommitTimeAnalysisPort`, `GetLanguageDiversityPort`, `GetActivityTrendPort`; Port/Out `UserMetricsQueryPort` ilgili *Info metotları.
- [x] **T1.5** ApiMapper: `RepositoryMetricsApiMapper` (tüm repository metrikleri), `UserMetricsApiMapper` (tüm user metrikleri); API katmanında kullanılıyor.
- [x] **T1.6** Controller: RepositoryMetricsController tüm endpoint’ler Port/In + ApiMapper kullanıyor (sadece al/ver). MetricsController user endpoint’leri Port/In + ApiMapper kullanıyor.

### Faz 2 – Port/Out ve Ham Veri

- [ ] **T2.1** Domain input tipleri (raw): OverviewRaw, PrListRaw, CommitListRaw, ReviewListRaw, BranchListRaw, LanguageListRaw vb. (saf Java record/class, anotasyonsuz).
- [ ] **T2.2** Port/Out: `RepositoryMetricsDataPort`, `UserMetricsDataPort` – ham veri sağlayan metotlar; konum `application/port/out/`.
- [ ] **T2.3** Adapter: RepositoryMetricsDataAdapter, UserMetricsDataAdapter – Port/Out implement; JPA’dan veri çeker, entity → raw map eder; hesaplama yok.

### Faz 3 – Use Case ve Domain Hesaplama

- [x] **T3.0** Domain paketi oluşturuldu: `com.optimetric.metrics.domain` – `domain.model` (PrRaw, PrMetricsInput, PrMetricsResult), `domain.service` (PrMetricsCalculator). Tüm iş kuralları ve hesaplamalar domain’de, saf Java, anotasyon yok.
- [x] **T3.1** Domain calculator – PR metrikleri: PrMetricsCalculator.calculate(PrMetricsInput) → PrMetricsResult. Adapter ham veriyi PrRaw listesine çevirip domain’e veriyor; hesaplama domain’de.
- [ ] **T3.1b** Diğer domain calculator’lar (saf Java): CommitMetricsCalculator, ActivityTrendCalculator, CodeQualityCalculator, …; Repository ve User için ayrı calculator’lar.
- [ ] **T3.2** Use case’ler: GetRepositoryMetricsUseCase, GetOverviewUseCase, … GetUserMetricsUseCase, GetPrMergeRateUseCase, …; Port/In implement; validasyon + Port/Out çağrısı + domain çağrısı + Application DTO map.
- [ ] **T3.3** Controller ve Port/In bağlantısı: Controller sadece Port/In (use case) çağırır; ApiMapper ile Application DTO → API DTO.

### Faz 4 – Domain Saf Java, Okunabilir

- [ ] **T4.1** Tüm hesaplama mantığı adapter’dan domain calculator’lara taşındı; domain’de anotasyon yok.
- [ ] **T4.2** Domain test’leri: calculator’lar tek başına unit test ile test edilebilir (parametre ver, sonuç assert et).

### Faz 5 – Eski Port/Adapter Temizliği

- [ ] **T5.1** `RepositoryMetricsServicePort`, `UserMetricsServicePort` kaldırıldı.
- [ ] **T5.2** Eski “hesaplama yapan” adapter kodu kaldırıldı; sadece Port/Out (data) adapter + use case + domain kullanılıyor.
