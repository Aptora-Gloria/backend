# Repo Sync Modülü — Tasarım

## Genel Bakış

Repo Sync Modülü, tek sorumluluğu **kullanıcının seçtiği repo için sync tetiklemek** ve o repo için **hazırlığı (ve isteğe bağlı durumu) sunmak** olan yeni bir hexagonal modüldür. Yeni entity veya tablo eklemez; mevcut GitHub entity'lerini (Repository, Branch, Commit, PullRequest, Language, SyncJob) kullanır ve sync'i, altyapı adapter'ları tarafından implemente edilen dışa dönük port'lar üzerinden yönetir; adapter'lar mevcut GitHub servislerine (örn. analyze/enqueue, readiness) delegasyon yapar.

**Referans:** [requirements.md](./requirements.md), [GITHUB_SYNC_MODULE_ANALYSIS.md](../../../docs/GITHUB_SYNC_MODULE_ANALYSIS.md)

## Mimari

### Katmanlar (hexagonal, auth tarzı)

| Katman          | Sorumluluk | Bağımlı olduğu |
|-----------------|------------|-----------------|
| **api**         | REST controller(lar), request/response DTO'ları, validasyon | application (use case'ler) |
| **application** | Use case'ler, port'lar (out), DTO'lar | domain + yalnızca port arayüzleri |
| **domain**      | Sync kuralları (seçilen repo için neyin ne zaman kuyruğa alınacağı) | hiçbir şey (saf) veya application DTO'ları |
| **infrastructure** | Port implementasyonları (enqueue, readiness, repo arama) | mevcut GitHub servisleri/adapter'ları, DB |

Application ve domain, altyapıya veya mevcut GitHub paket iç yapısına **bağımlı değildir**; yalnızca port arayüzlerine bağımlıdır.

### Paket yapısı (önerilen)

```
com.optimetric.reposync
├── api
│   ├── RepoSyncController.java          # örn. POST /sync/trigger, POST /sync/readiness
│   ├── request/
│   │   ├── TriggerSyncRequest.java      # fullName
│   │   └── ReadinessRequest.java        # fullName
│   └── response/
│       ├── TriggerSyncResponse.java    # status: TRIGGERED | ALREADY_READY | ...
│       └── ReadinessResponse.java      # state, missing[], flags (veya mevcut DTO kullan)
├── application
│   ├── port
│   │   └── out
│   │       ├── TriggerSyncPort.java     # (login, fullName) için sync tetikle / kuyruğa al
│   │       └── ReadinessPort.java       # (fullName) için hazırlık al -> state, flags
│   ├── usecase
│   │   ├── TriggerSyncForRepoUseCase.java
│   │   └── GetRepoReadinessUseCase.java
│   └── dto
│       ├── TriggerSyncCommand.java     # login, fullName
│       ├── TriggerSyncResult.java      # status, message
│       └── RepoReadinessResult.java    # state, missing, flags (veya mevcut kullan)
├── domain
│   └── service
│       └── RepoSyncDomainService.java  # kural: "bu repo için sync kuyruğa alındığından emin ol"
└── infrastructure
    └── adapter
        ├── TriggerSyncAdapter.java     # TriggerSyncPort; mevcut analyze/enqueue çağırır
        └── ReadinessAdapter.java        # ReadinessPort; mevcut RepoSyncStatusService çağırır
```

Mevcut GitHub paketleri değişmez; adapter'lar `reposync.infrastructure` altında yer alır ve yeni modülün GitHub api katmanına bağımlı olmaması için mevcut `RepositoryAnalyzeServicePort`, `RepoSyncStatusServicePort` vb. kullanılır.

## Port'lar (dışa dönük)

### TriggerSyncPort

- **Amaç:** Seçilen repo için sync tetikleyerek gerekli tüm verinin (branch, commit, PR, dil) kuyruğa alınmasını veya sync için işaretlenmesini sağlamak.
- **Girdi:** `login` (String), `fullName` (String).
- **Çıktı:** Sync'in tetiklenip tetiklenmediği, zaten hazır mı veya hata (örn. repo bulunamadı) bilgisini taşıyan sonuç.
- **Implementasyon:** Adapter mevcut “analyze” veya eşdeğer enqueue mantığını çağırır (örn. `RepositoryAnalyzeServicePort.analyze(login, fullName)`). Yeni job tipi gerekmez; mevcut BRANCH_SYNC, COMMIT_SYNC, PR_LIST, LANGUAGE_SYNC, PR_DETAIL, PR_REVIEWS kullanılır.

### ReadinessPort

- **Amaç:** Repo için hazırlık durumu ve bileşen bayraklarını döndürmek.
- **Girdi:** `fullName` (String).
- **Çıktı:** Durum (READY / PARTIAL / NOT_READY), eksik bileşen listesi, bayraklar (branchesReady, commitsReady vb.).
- **Implementasyon:** Adapter mevcut hazırlık servisini çağırır (örn. `RepoSyncStatusServicePort.getReadiness(fullName)`); gerekirse modül DTO'suna map eder veya mevcut DTO'yu kullanır.

## Use case'ler

### TriggerSyncForRepoUseCase

- **Girdi:** TriggerSyncCommand(login, fullName).
- **Akış:**
  1. İsteğe bağlı: bir port üzerinden repo varlığı / kullanıcı erişimi doğrula (veya çağıran doğrulamış kabul edilir; gerekirse RepoResolverPort eklenir).
  2. Domain servisini çağır: “bu repo için sync tetiklendiğinden emin ol.”
  3. Domain servisi (veya use case) TriggerSyncPort.trigger(login, fullName) çağırır.
  4. TriggerSyncResult(status, message) döndür.
- **Çıktı:** TriggerSyncResult (örn. TRIGGERED, ALREADY_READY, REPO_NOT_FOUND).

### GetRepoReadinessUseCase

- **Girdi:** fullName.
- **Akış:**
  1. ReadinessPort.getReadiness(fullName) çağır.
  2. RepoReadinessResult (state, missing, flags) döndür.
- **Çıktı:** RepoReadinessResult (veya mevcut RepositoryReadinessDto).

## Domain

### RepoSyncDomainService (isteğe bağlı)

- **Sorumluluk:** “Seçilen repo için sync tetiklendiğinden emin ol” kuralını kodlamak. İnce olabilir: örn. “TriggerSyncPort çağır”; veya “zaten READY ise kuyruğa alma” gibi mantık içerebilir.
- **Bağımlılıklar:** Altyapıya bağımlılık yok; use case'den port arayüzü alır (veya use case port'u doğrudan çağırır ve MVP'de domain atlanır).

MVP için use case TriggerSyncPort'u doğrudan çağırabilir; kurallar büyüdükçe domain servisi eklenebilir (örn. “son N dakikada sync çalıştıysa kuyruğa alma”).

## API (REST)

### Endpoint'ler (önerilen)

| Method | Path (örnek) | Request body | Response | Amaç |
|--------|----------------|--------------|----------|------|
| POST   | `/api/reposync/trigger` | `{ "fullName": "owner/repo" }` | `{ "status": "TRIGGERED" \| "ALREADY_READY" \| "REPO_NOT_FOUND", "message": "..." }` | Seçilen repo için tek tetikleyici (Req1). |
| POST   | `/api/reposync/readiness` | `{ "fullName": "owner/repo" }` | `{ "state", "missing", "flags" }` (mevcut sync/status ile aynı yapı) | Seçilen repo için hazırlık (Req5). |

- **Kimlik doğrulama:** Mevcut `@AuthenticationPrincipal String login` kullanılır; controller login ve fullName'i use case'lere iletir.
- **Validasyon:** fullName zorunlu (örn. `@NotBlank`).

Mevcut endpoint'ler (örn. `/api/github/repositories/analyze`, `/api/github/repositories/sync/status`) kalabilir; frontend'in init controller'lara ihtiyaç duymaması için yeni modül endpoint'leri kullanması teşvik edilir.

## Veri akışı

### Tetikleme akışı

1. İstemci `{ "fullName": "owner/repo" }` ile `POST /api/reposync/trigger` gönderir.
2. Controller doğrular, güvenlik bağlamından login alır, `TriggerSyncForRepoUseCase.execute(TriggerSyncCommand(login, fullName))` çağırır.
3. Use case `TriggerSyncPort.trigger(login, fullName)` çağırır.
4. Adapter port'u mevcut `RepositoryAnalyzeServicePort.analyze(login, fullName)` (veya eşdeğeri) çağırarak implemente eder. Mevcut mantık gerektiğinde BRANCH_SYNC, COMMIT_SYNC, PR_LIST, LANGUAGE_SYNC, PR_DETAIL, PR_REVIEWS kuyruğa alır.
5. Adapter analyze yanıtını TriggerSyncResult'a map eder (örn. SYNC_STARTED → TRIGGERED, ALREADY_READY → ALREADY_READY).
6. Controller 200 ve TriggerSyncResponse döndürür; ağır iş mevcut runner'da asenkron çalışır.

### Hazırlık akışı

1. İstemci `{ "fullName": "owner/repo" }` ile `POST /api/reposync/readiness` gönderir.
2. Controller `GetRepoReadinessUseCase.execute(fullName)` çağırır.
3. Use case `ReadinessPort.getReadiness(fullName)` çağırır.
4. Adapter port'u mevcut `RepoSyncStatusServicePort.getReadiness(fullName)` çağırarak implemente eder; aynı DTO veya map edilmiş sonuç döndürür.
5. Controller 200 ve hazırlık yanıtı döndürür.

## Mevcut bileşenlerin kullanımı

| Yeni modül ihtiyacı | Kullanılan mevcut bileşen |
|---------------------|----------------------------|
| Sync job'ları kuyruğa alma | `RepositoryAnalyzeServicePort.analyze(login, fullName)` (veya eşdeğeri) |
| Hazırlık             | `RepoSyncStatusServicePort.getReadiness(fullName)` |
| Entity / tablolar    | Tüm mevcut GitHub entity ve tabloları; yenisi yok. |
| Job çalıştırma       | Mevcut SyncJobRunner ve job tipleri (BRANCH_SYNC, COMMIT_SYNC, PR_LIST vb.). |

`reposync.infrastructure` içindeki adapter'lar bu port/servislere bağımlıdır; yeni modülün application/domain katmanları yalnızca TriggerSyncPort ve ReadinessPort'a bağımlıdır.

## Hata yönetimi

- **Repo bulunamadı:** TriggerSyncPort veya use case REPO_NOT_FOUND döndürür; controller API sözleşmesine göre 404 veya 200 + status REPO_NOT_FOUND döner.
- **Kullanıcı bulunamadı / yetkisiz:** Mevcut güvenlik ve use case öncesi isteğe bağlı validasyonla ele alınır; adapter exception fırlatabilir; 401/403'e map edilir.
- **Analyze / readiness hatası:** Adapter yakalar veya iletir; use case sonuca map eder; controller uygun HTTP kodu ve mesaj döner.

## Test stratejisi

- **Birim testleri:** Use case'ler mock TriggerSyncPort ve ReadinessPort ile; domain servisi (varsa) mock port ile.
- **Adapter testleri:** TriggerSyncAdapter ve ReadinessAdapter, mock RepositoryAnalyzeServicePort ve RepoSyncStatusServicePort ile; delegasyon ve map doğrulanır.
- **Entegrasyon testleri:** İsteğe bağlı; test DB ve mevcut GitHub modülü üzerinden adapter'lar aracılığıyla gerçek analyze ve readiness çağrılabilir.

## Özet

| Öğe         | Tasarım kararı |
|-------------|-----------------|
| **Modül adı** | Repo Sync Modülü (`com.optimetric.reposync`) |
| **Port'lar**  | TriggerSyncPort, ReadinessPort (dışa dönük) |
| **Use case'ler** | TriggerSyncForRepoUseCase, GetRepoReadinessUseCase |
| **API**       | POST /trigger, POST /readiness (minimal) |
| **Entity'ler** | Yeni yok; mevcut GitHub entity'leri kullanılır |
| **Adapter'lar** | Mevcut analyze ve readiness servislerine delegasyon |

Bu tasarım Req1–Req6'yı karşılar: tek tetikleyici, mevcut job'larla eksiksiz veri, hızlı yanıt, hexagonal yapı, hazırlık ve entity/şema değişikliği yok.
