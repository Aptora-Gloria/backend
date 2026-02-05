# Domain’e Taşıma Planı (Hexagonal – İş Kuralları)

Bu doküman, adapter ve mapper’da bulunan iş kurallarının domain katmanına taşınması için adım adım planı içerir.

## Hedef

- **İş kuralları (business rules)** domain’de olsun; adapter sadece teknik iş (execute, persistence, API çağrısı) yapsın.
- **Execute** metotları adapter’da kalsın.
- Domain sadece pure logic; I/O yok, framework yok.

---

## Faz 1: Domain tipleri ve SyncJobPolicy

### 1.1 SyncJobSnapshot (domain value object)

- **Paket:** `com.optimetric.github.domain.vo` (veya `domain.model`)
- **Amaç:** Domain’in application/infrastructure’a bağımlı olmaması için sync job’a dair sadece karar için gerekli alanlar.
- **Alanlar:** `SyncJobStatus status`, `OffsetDateTime startedAt`, `OffsetDateTime completedAt`, `int attemptCount`
- **Kullanım:** Adapter/use case `SyncJobInfo` → `SyncJobSnapshot` dönüşümü yapar; domain sadece `SyncJobSnapshot` görür.

### 1.2 SyncJobPolicy (domain service)

- **Paket:** `com.optimetric.github.domain.service`
- **Metotlar (pure, I/O yok):**
  - `canStartSync(List<SyncJobSnapshot> jobs)` – Aynı repo için RUNNING/PENDING var mı?
  - `isJobStuck(SyncJobSnapshot job, OffsetDateTime now, int stuckThresholdMinutes)`
  - `shouldScheduleRetry(SyncJobSnapshot job, int maxAttempts)` – FAILED ve attemptCount < max
  - `retryDelayMinutes(SyncJobSnapshot job, int baseDelayMinutes)` – Gecikme süresi
  - `shouldCleanup(SyncJobSnapshot job, OffsetDateTime cutoffTime)` – Eski job silinebilir mi?
  - `determineOverallSyncStatus(SyncJobSnapshot... jobs)` – RUNNING > FAILED > COMPLETED > PENDING
  - `getLastSyncTime(SyncJobSnapshot... jobs)` – En son completedAt
- **Sabitler:** MAX_RETRY_ATTEMPTS, RETRY_DELAY_MINUTES, STUCK_JOB_THRESHOLD_MINUTES policy içinde (veya parametre).

---

## Faz 2: PR ile ilgili domain kuralları

### 2.1 PullRequestSyncPolicy

- **Paket:** `com.optimetric.github.domain.service`
- **Metot:** `needsSync(OffsetDateTime lastSyncedAt, OffsetDateTime updatedAt)` – PR yeniden sync edilmeli mi?

### 2.2 DataLevelPolicy

- **Paket:** `com.optimetric.github.domain.service`
- **Metot:** `resolveDataLevel(boolean hasChangeStats, boolean hasReviews, boolean hasCommits)` – BASIC / FULL (şimdilik sadece hasChangeStats kullanılabilir).

---

## Faz 3: Adapter ve mapper güncellemeleri

### 3.1 SyncJobServiceAdapter

- `SyncJobPolicy` inject et.
- `updateJobStatus`: FAILED iken retry kararı ve gecikme için `shouldScheduleRetry`, `retryDelayMinutes` kullan.
- `resetStuckJobs`: Hangi job’ların stuck sayılacağı için `isJobStuck` kullan.
- `cleanupOldJobs`: Hangi job’ların silineceği için `shouldCleanup` kullan (veya mevcut repository sorgusu + domain kuralı).
- Execute metotları ve create/update/pagination aynı yerde kalır.

### 3.2 SyncJobMapper (infrastructure)

- `determineOverallSyncStatus(SyncJobInfo... jobs)` → `SyncJobPolicy.determineOverallSyncStatus(snapshots)` (önce Info → Snapshot dönüşümü adapter/caller’da veya mapper’da).
- `getLastSyncTime(SyncJobInfo... jobs)` → `SyncJobPolicy.getLastSyncTime(snapshots)`.
- Entity overload’ları: Önce `toSyncJobInfo` ile Info’ya çevir, sonra Snapshot’a çevirip policy’e ver; veya sadece Info overload’ını kullanıp entity kullanan yerleri Info’ya geçir.

### 3.3 PullRequestServiceAdapter

- `SyncJobPolicy` inject et.
- `initiatePullRequestSync`: Job’ları al, `SyncJobPolicy.canStartSync(snapshots)`; false ise throw, true ise create + execute.
- `getSyncStatus`: `SyncJobPolicy.determineOverallSyncStatus` ve `getLastSyncTime` kullan (Snapshot’a mapper ile).
- `needsSynchronization`: `PullRequestSyncPolicy.needsSync(lastSyncedAt, updatedAt)` çağır.
- `updateDataLevel`: `DataLevelPolicy.resolveDataLevel(...)` ile yeni level’ı al, entity’yi güncelle.

---

## Faz 4: Konfigürasyon ve testler

### 4.1 DomainServiceConfig

- `SyncJobPolicy`, `PullRequestSyncPolicy`, `DataLevelPolicy` bean olarak ekle.

### 4.2 MapperTest

- `determineOverallSyncStatus` ve `getLastSyncTime` testleri `SyncJobPolicy` üzerinden yapılacak şekilde güncelle (veya policy’yi test et).

---

## Uygulama sırası (kısa)

1. Domain: `SyncJobSnapshot` + `SyncJobPolicy`
2. Domain: `PullRequestSyncPolicy` + `DataLevelPolicy`
3. `DomainServiceConfig`: yeni bean’ler
4. `SyncJobServiceAdapter`: policy kullanımı
5. `SyncJobMapper`: policy’e delegate (veya doğrudan adapter’da policy kullan, mapper’dan kaldır)
6. `PullRequestServiceAdapter`: policy kullanımı
7. Testler

---

## Tamamlanan uygulama (özet)

- **Domain:** `SyncJobSnapshot`, `SyncJobPolicy`, `PullRequestSyncPolicy`, `DataLevelPolicy` eklendi.
- **SyncJobInfo:** `toSnapshot()` eklendi.
- **SyncJobServiceAdapter:** Retry/stuck/cleanup kararları `SyncJobPolicy` ile; sabitler policy'den.
- **SyncJobMapper:** `determineOverallSyncStatus` ve `getLastSyncTime` domain policy'e delegate.
- **PullRequestServiceAdapter:** `canStartSync`, `PullRequestSyncPolicy.needsSync`, `DataLevelPolicy.resolveDataLevel` kullanılıyor.
- **Testler:** MapperTest ve bazı adapter testleri domain enum import'larına geçirildi.
- **Derleme:** `mvn compile -DskipTests` başarılı. Bazı mevcut testler bu taşımadan bağımsız hatalar içeriyor.

---

## Notlar

- Domain, application DTO’ya bağımlı olmasın diye `SyncJobSnapshot` domain’de tanımlanır; dönüşüm adapter veya application tarafında yapılır.
- Execute metotları (`executePrListJob`, `executePrDetailJob`, vb.) adapter’da kalır; sadece “ne zaman retry”, “stuck mı”, “sync başlatılabilir mi” gibi kararlar domain’e taşınır.
