# Repo Sync Modülü — Uygulama Görevleri

**Referans:** [requirements.md](./requirements.md), [design.md](./design.md)

Görevler port'lar ve use case'ler önce, sonra adapter'lar, sonra API olacak şekilde sıralanmıştır. Entity ve şema işi yoktur (mevcut GitHub entity'leri kullanılır).

---

## Faz 1 — Application katmanı (port'lar, DTO'lar, use case'ler)

- [x] **1. Application DTO'larını oluştur**
  - `TriggerSyncCommand` (login, fullName) oluştur.
  - `TriggerSyncResult` (status enum: TRIGGERED, ALREADY_READY, SYNC_ALREADY_RUNNING, REPO_NOT_FOUND; isteğe bağlı message) oluştur.
  - `RepoReadinessResult` (state, missing, flags) oluştur veya mevcut `RepositoryReadinessDto` kullanımını tanımla.
  - _Gereksinimler: Req1, Req3, Req5_

- [x] **2. Dışa dönük port'ları tanımla**
  - `TriggerSyncPort`: `TriggerSyncResult trigger(String login, String fullName)`.
  - `ReadinessPort`: `RepoReadinessResult getReadiness(String fullName)` (veya mevcut DTO tipi döndür).
  - _Gereksinimler: Req1, Req4, Req5_

- [x] **3. Use case'leri implemente et**
  - `TriggerSyncForRepoUseCase`: execute(TriggerSyncCommand) → TriggerSyncResult; TriggerSyncPort çağır.
  - `GetRepoReadinessUseCase`: execute(String fullName) → RepoReadinessResult; ReadinessPort çağır.
  - _Gereksinimler: Req1, Req3, Req5_

- [x] **4. (İsteğe bağlı) Domain servisi**
  - Mantık büyürse “repo için sync tetiklendiğinden emin ol” kuralıyla `RepoSyncDomainService` oluştur; aksi halde use case port'u doğrudan çağırır.
  - _Gereksinimler: Req4_

---

## Faz 2 — Infrastructure (adapter'lar)

- [x] **5. TriggerSyncPort adapter'ını implemente et**
  - `TriggerSyncPort` implemente eden `TriggerSyncAdapter` oluştur.
  - Mevcut `RepositoryAnalyzeServicePort.analyze(login, fullName)` (veya eşdeğeri) çağır.
  - Analyze yanıtını (SYNC_STARTED, ALREADY_READY, SYNC_ALREADY_RUNNING) TriggerSyncResult'a map et; RepositoryNotFoundException → REPO_NOT_FOUND.
  - _Gereksinimler: Req1, Req2, Req4, Req6_

- [x] **6. ReadinessPort adapter'ını implemente et**
  - `ReadinessPort` implemente eden `ReadinessAdapter` oluştur.
  - Mevcut `RepoSyncStatusServicePort.getReadiness(fullName)` çağır.
  - RepoReadinessResult'a map et veya mevcut DTO'yu olduğu gibi döndür.
  - _Gereksinimler: Req2, Req4, Req5, Req6_

- [x] **7. Adapter'ları Spring'de bağla**
  - TriggerSyncAdapter ve ReadinessAdapter'ı TriggerSyncPort ve ReadinessPort implementasyonları olarak kaydet (örn. `@Service` + `@Primary` veya qualified bean).
  - Mevcut GitHub bean'lerinin (RepositoryAnalyzeServicePort, RepoSyncStatusServicePort) kullanılabilir olduğundan ve adapter'lara enjekte edildiğinden emin ol.
  - _Gereksinimler: Req4_

---

## Faz 3 — API katmanı

- [x] **8. API request/response DTO'larını oluştur**
  - `TriggerSyncRequest` (fullName, validasyon örn. @NotBlank).
  - `TriggerSyncResponse` (status, message).
  - `ReadinessRequest` (fullName); `ReadinessResponse` için mevcut hazırlık yanıtını kullan veya sarmala.
  - _Gereksinimler: Req1, Req5_

- [x] **9. REST controller'ı implemente et**
  - `com.optimetric.reposync.api` (veya seçilen paket) altında `RepoSyncController` oluştur.
  - `POST /api/reposync/trigger`: body TriggerSyncRequest, `@AuthenticationPrincipal String login`; TriggerSyncForRepoUseCase çağır; TriggerSyncResponse döndür (200; sözleşmeye göre REPO_NOT_FOUND ise 404).
  - `POST /api/reposync/readiness`: body ReadinessRequest; GetRepoReadinessUseCase çağır; hazırlık yanıtı döndür (200; repo bulunamazsa 404).
  - Request validasyonu ve hata yönetimi ekle (örn. UserNotFoundException, RepositoryNotFoundException).
  - _Gereksinimler: Req1, Req3, Req5_

- [x] **10. Güvenlik ve base path**
  - `/api/reposync/**` yolunun mevcut JWT/auth ile korunduğundan ve `@AuthenticationPrincipal` ile login sağlandığından emin ol.
  - Base path ve request/response'u OpenAPI veya dahili dokümanda belgele (uygulanabilirse).
  - _Gereksinimler: Req1_

---

## Faz 4 — Doğrulama ve doküman

- [ ] **11. Birim testleri**
  - Use case'ler: TriggerSyncPort ve ReadinessPort mock'la; doğru çağrı ve sonuç map'ini doğrula.
  - Adapter'lar: RepositoryAnalyzeServicePort ve RepoSyncStatusServicePort mock'la; delegasyon ve map'i doğrula.
  - Controller: use case'leri mock'la; HTTP kodu ve response body'yi doğrula.
  - _Gereksinimler: Req1–Req6_

- [ ] **12. Entegrasyon kontrolü**
  - Bilinen bir repo için `POST /api/reposync/trigger` çağır; job'ların kuyruğa alındığını doğrula (örn. DB veya mevcut runner logları).
  - `POST /api/reposync/readiness` çağır; yanıtın o repo için mevcut sync/status anlamıyla eşleştiğini doğrula.
  - _Gereksinimler: Req1, Req2, Req5_

- [ ] **13. Frontend/doküman güncelle**
  - Frontend el kitabına (örn. REPO_ANALYSIS_SYNC_INTEGRATION.md veya yeni “Repo Sync Modülü” bölümü) yeni endpoint'leri yaz: seçilen-repo akışı için legacy init/analyze yerine `/api/reposync/trigger` ve `/api/reposync/readiness` tercih edilsin.
  - _Gereksinimler: Req1, Req3_

---

## Özet

| Faz   | Görevler | Odak |
|-------|----------|------|
| **1** | 1–4   | Port'lar, DTO'lar, use case'ler (isteğe bağlı domain) |
| **2** | 5–7   | Mevcut GitHub servislerine delegasyon yapan adapter'lar; wiring |
| **3** | 8–10  | API DTO'ları, controller, güvenlik |
| **4** | 11–13 | Testler, entegrasyon kontrolü, doküman |

Yeni entity veya migration yok; tüm kalıcılık mevcut GitHub entity ve tablolarını kullanır.
