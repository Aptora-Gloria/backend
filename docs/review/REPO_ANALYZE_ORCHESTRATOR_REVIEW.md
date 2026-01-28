# Repo Analyze + Orchestrator Review (MVP)

## 1. Summary (What works / What doesn’t)

- **What works**
  - `POST /api/github/repositories/sync/status` DB-only readiness hesaplıyor ve `missing[]` sırası beklenen kurala uyuyor (`BRANCHES, PRS, PR_DETAILS, REVIEWS, COMMITS, LANGUAGES`).  
    - İlgili: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepoSyncStatusServiceAdapter.java`
  - `POST /api/github/repositories/analyze` readiness’e göre job type mapping yapıyor ve **commit için branch dependency** kontrolü var (branch yokken `COMMIT_SYNC` seçilmiyor).
    - İlgili: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepositoryAnalyzeServiceAdapter.java`
  - PR pipeline tarafında `PR_LIST` sonrası `PR_DETAIL/PR_REVIEWS/PR_COMMITS` tetikleniyor (job chain mevcut).
    - İlgili: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java`

- **What doesn’t / spec ile uyuşmayanlar**
  - **BLOCKER**: Analyze endpoint “GitHub API call yapmamalı” denmiş; ancak mevcut implementasyon `RepositoryAnalyzeServiceAdapter.executeJob()` içinde `BranchServicePort/CommitServicePort/LanguageServicePort` çağırıyor ve bunlar GitHub API çağrısı yapıyor. Bu, “DB + enqueue only” kuralını ihlal ediyor.
  - **MAJOR**: Analyze şu an job’ları “enqueue + hemen çalıştır (sync execution)” yapıyor. MVP kontratı “enqueue” olarak tariflenmişti (async job runner varsayımı). Şu haliyle endpoint latency/timeouts ve rate-limit riskini büyütüyor.
  - **MAJOR**: `RepoSyncStatusServiceAdapter.getReadiness(fullName)` **user/login access kontrolü yapmadan** `RepositoryRepository.findByFullName(fullName)` ile çalışıyor. Controller `@AuthenticationPrincipal` alsa da login kullanılmıyor → authorization gap / bilgi sızması riski.

## 2. API Contract Verification

### Analyze endpoint
- **Route**: `POST /api/github/repositories/analyze`
- **Request DTO**: `{"fullName": "owner/repo"}`
  - `src/main/java/com/optimetric/github/api/request/RepositoryAnalyzeRequest.java`
- **Response DTO**: 
  - `{"state":"SYNC_STARTED|ALREADY_READY|SYNC_ALREADY_RUNNING","enqueued":[...],"statusEndpoint":"/api/github/repositories/sync/status"}`
  - `src/main/java/com/optimetric/github/api/response/RepositoryAnalyzeResponse.java`
- **READY davranışı**: `AnalyzeState.ALREADY_READY` dönüyor.
  - `RepositoryAnalyzeServiceAdapter.analyze(...)`
- **Repo bulunamazsa 404**:
  - `RepoSyncStatusServiceAdapter.getReadiness()` `RepositoryNotFoundException` fırlatıyor.
  - `GlobalExceptionHandler` bunu `404 NOT_FOUND`’a map ediyor.
  - İlgili: `src/main/java/com/optimetric/shared/exception/GlobalExceptionHandler.java`

### Sync status endpoint
- **Route**: `POST /api/github/repositories/sync/status`
- **Request DTO**: `{"fullName":"owner/repo"}`
  - `src/main/java/com/optimetric/github/api/request/RepositorySyncStatusRequest.java`
- **Response DTO**: `RepositoryReadinessDto(state, missing, flags)`
  - `src/main/java/com/optimetric/github/api/response/RepositoryReadinessDto.java`
- **Repo bulunamazsa 404**: `RepositoryNotFoundException` → `GlobalExceptionHandler`

## 3. DB & Job Enqueue Verification

### İlgili sınıf/dosya listesi (review scope)

- **Controllers**
  - `src/main/java/com/optimetric/github/api/RepositoryAnalyzeController.java`
  - `src/main/java/com/optimetric/github/api/RepositorySyncStatusController.java`
- **Services / Adapters**
  - `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepositoryAnalyzeServiceAdapter.java`
  - `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepoSyncStatusServiceAdapter.java`
  - `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/SyncJobServiceAdapter.java`
  - `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/PullRequestServiceAdapter.java` (PR sync başlatma + sync status)
- **Repositories (JPA)**
  - `src/main/java/com/optimetric/github/infrastructure/persistence/jpa/SyncJobRepository.java`
  - `src/main/java/com/optimetric/github/infrastructure/persistence/jpa/RepositoryRepository.java` (readiness/analyze access)
  - readiness EXISTS query repository’leri (Branch/Commit/PR/Review/Language) — ilgili yerler `RepoSyncStatusServiceAdapter`
- **Entities / Enums**
  - `src/main/java/com/optimetric/github/infrastructure/persistence/entity/SyncJobEntity.java`
  - `src/main/java/com/optimetric/github/infrastructure/persistence/entity/SyncJobType.java`
  - `src/main/java/com/optimetric/github/infrastructure/persistence/entity/SyncJobStatus.java`

### Job enqueue davranışı (mevcut kod)

- `RepositoryAnalyzeServiceAdapter`
  - “repo için herhangi bir PENDING/RUNNING job varsa” erken çıkış yapıyor (`hasActiveJobs`) → repo-level lock davranışı.
  - Sonra `missing[]` üzerinden mapping:
    - `BRANCHES -> BRANCH_SYNC`
    - `PRS -> PR_LIST`
    - `LANGUAGES -> LANGUAGE_SYNC`
    - `COMMITS -> COMMIT_SYNC` (branch ready değilse skip)
  - İdempotency kontrolü: `repo_full_name + job_type` için **PENDING** ve **RUNNING** kontrolü var.
  - Not: `PR_LIST` için özel case var: `executePrListJob()` çağrılıyor (bu method kendi içinde job create/find yapıyor).

## 4. Idempotency & Concurrency Risks

### En kritik 5 risk (severity)

1) **BLOCKER** — Analyze endpoint içinde GitHub API çağrısı yapılması  
   - Spec: “Analyze endpoint içinde GitHub API çağrısı olmamalı; sadece DB + enqueue.”  
   - Mevcut: `RepositoryAnalyzeServiceAdapter.executeJob()` → `branchServicePort.initRepoBranches()`, `commitServicePort.initCommits()`, `languageServicePort.initRepoLanguages()` → GitHub API çağırıyor.  
   - Etki: endpoint “orchestrator” olmaktan çıkıp “init endpoint’lerini çağıran” bir endpoint oluyor; latency, rate limit, failure surface artar; FE yanlış beklentiye girer.

2) **MAJOR** — Repo-level “active job” gate idempotency’yi aşırı kısıtlıyor  
   - `hasActiveJobs`: repo’da herhangi bir PENDING/RUNNING job varsa **tüm analyze** `SYNC_ALREADY_RUNNING` dönüyor.  
   - Risk: Bir job stuck/pending kalırsa repo sonsuza kadar kilitlenir; ayrıca bağımsız job’ların (ör. language) tekrar enqueue edilmesi engellenir.

3) **MAJOR** — Race condition: “check then insert” (unique constraint yok)  
   - `github_sync_jobs` tablosunda `(repo_full_name, job_type, status)` veya “active unique” constraint görünmüyor.  
   - İki concurrent analyze çağrısı aynı anda “existingJob empty” görüp iki job create edebilir.  
   - Öneri: DB-level uniqueness (partial unique index) veya `INSERT ... ON CONFLICT DO NOTHING` pattern.

4) **MAJOR** — `sync/status` endpoint’inde login kullanılmıyor (authorization gap)  
   - Controller login alıyor ama service `getReadiness(fullName)` user check yapmıyor.  
   - Risk: Kullanıcı başka kullanıcının repo readiness bilgisini görebilir (repo visibility modeline bağlı).

5) **MINOR** — `COMMIT_SYNC` “since” parametresi job’a taşınmıyor  
   - `SyncJobEntity` içinde time-window/payload alanı yok (yalnızca `cursor/current_page`).  
   - Şu an “90 gün” runtime hesaplanıyor; job tekrar çalıştırıldığında farklı `since` olur; audit/debug zorlaşır.  
   - Öneri: `cursor` alanını “payload JSON” gibi kullanmak (MVP hack) veya yeni `payload` kolonu (daha doğru ama migration ister).

### Minimal fix önerileri (pseudo-code)

- **DB-level idempotency (önerilen)**:

```sql
-- Postgres örneği: aynı repo+job_type için aktif job tek olsun
CREATE UNIQUE INDEX IF NOT EXISTS ux_sync_jobs_active
ON github_sync_jobs (repo_full_name, job_type)
WHERE status IN ('PENDING','RUNNING');
```

- **Code-level (transactional) idempotency**:

```java
// pseudo
@Transactional
if (!syncJobRepo.existsActive(repo, type)) {
  try { syncJobService.createJob(type, repo); }
  catch (DataIntegrityViolationException ignore) { /* concurrent create */ }
}
```

## 5. Ordering / Dependency Checks (Branch -> Commit etc.)

- **Branch -> Commit**
  - ✅ `RepoSyncStatusServiceAdapter` `branchesReady` false iken `COMMIT_SYNC` enqueue edilmiyor.
  - ⚠️ Ancak branch sync tamamlandıktan sonra commit sync otomatik tetiklenmiyor (orchestrator async ise) → ikinci analyze veya scheduler gerekir.
  - Ayrıca commit ingestion branch’e FK bağlıysa, branch sync’in “defaultBranch/branch list” ile uyumu doğrulanmalı.

- **Repo default branch fallback**
  - `RepositoryAnalyzeServiceAdapter` default branch boşsa `"main"` fallback yapıyor.
  - Risk: default branch `"master"`/custom ise commit init başarısız olur (`BranchNotFoundException`) veya yanlış branch’e gider.
  - Öneri: fallback yerine `repositories.default_branch` null ise önce `BRANCH_SYNC` sonra DB’deki default branch’i kullan.

## 6. PR Pipeline Checks (PR_LIST -> PR_DETAIL -> PR_REVIEWS)

- `SyncJobServiceAdapter.executePrListJob()`:
  - GitHub’dan PR list çeker, upsert eder.
  - Ardından `scheduleDetailJobs(repository)` çağırır.
- `scheduleDetailJobs()`:
  - `pullRequestRepository.findPullRequestsWithoutChangeStats(repository)` ile PR_DETAIL ihtiyacını bulur.
  - Her PR için sırasıyla:
    - `executePrDetailJob(...)`
    - `executePrReviewsJob(...)`
    - `executePrCommitsJob(...)`
  - Bu job’lar **hemen execute** ediliyor (async queue değil).

**Edge-case önerileri**
- Aynı PR için eş zamanlı detail/review job’ları duplicate olabilir (özellikle `findPullRequestsWithoutChangeStats` sonucu genişse).
- `executePrDetailJob` ve `executePrReviewsJob` idempotent upsert yaptığı için veri tarafında idare eder; ama job tablosunda gereksiz şişme/aktif job sayısı artabilir.
- Öneri: PR_DETAIL/PR_REVIEWS job create ederken `(repo_full_name, job_type, cursor=pullRequestId)` gibi bir unique/guard (MVP-level).

## 7. Missing Tests (Postman + automated)

### Postman / manual
- **Analyze spec test (DB-only expectation)**: Analyze çağrısında GitHub’a outbound call olmamalı (şu an ihlal). Bunu doğrulamak için outbound client mock/spy veya log assertion önerilir.
- **Idempotency test**: aynı repo için arka arkaya 3 kez analyze → ikinci/üçüncü çağrıda duplicate job satırı oluşmamalı.
- **Concurrency test**: 2 paralel analyze request → aktif job sayısı 1 olmalı (veya DB constraint ile deterministik).

### Automated (JUnit)
- `RepositoryAnalyzeServiceAdapter` için slice test:
  - `RepoSyncStatusServicePort` mock ile `missing` varyasyonları test edilir (branch missing, commits missing vs).
  - `SyncJobRepository` H2/Postgres testcontainer ile “active unique” davranışı doğrulanır.
- `RepoSyncStatusServiceAdapter` için integration test:
  - readiness flags/missing order deterministic mi?

## 8. Recommended Fixes (Prioritized checklist)

- [ ] **(BLOCKER)** Analyze endpoint’i “DB + enqueue only” olacak şekilde düzelt: `executeJob()` çağrılarını kaldır; sadece `createJob(...)` ile job üret.
- [ ] **(MAJOR)** `sync/status` için user access kontrolü ekle (login ile `findByFullNameAndUser(...)` veya domain service check). Controller’da login alınmış ama kullanılmıyor.
- [ ] **(MAJOR)** DB-level idempotency: aktif job için unique index (partial) ekle veya transactional “insert-if-absent” çözümü uygula.
- [ ] **(MAJOR)** Repo-level `hasActiveJobs` gate’i job-type bazlı yap (tüm repo yerine yalnızca ilgili job type aktifse skip).
- [ ] **(MINOR)** `COMMIT_SYNC` parametrelerini (since/branch) job’da izlenebilir hale getir: minimum çözüm `cursor` içine JSON payload yazmak (`{"branch":"x","since":"..."}`).

## 9. Quick Manual Verification Steps (SQL + curl samples)

### SQL doğrulama sorguları (en az 5)

```sql
-- 1) Repo için job durum dağılımı
SELECT repo_full_name, job_type, status, COUNT(*) cnt
FROM github_sync_jobs
WHERE repo_full_name = 'Aptora-Gloria/frontend'
GROUP BY 1,2,3
ORDER BY job_type, status;
```

```sql
-- 2) Aktif job var mı? (idempotency kontrol)
SELECT *
FROM github_sync_jobs
WHERE repo_full_name = 'Aptora-Gloria/frontend'
  AND status IN ('PENDING','RUNNING')
ORDER BY system_created_at DESC;
```

```sql
-- 3) Duplicate “active” job (repo_full_name + job_type) var mı?
SELECT repo_full_name, job_type, COUNT(*) AS active_cnt
FROM github_sync_jobs
WHERE status IN ('PENDING','RUNNING')
GROUP BY repo_full_name, job_type
HAVING COUNT(*) > 1;
```

```sql
-- 4) Readiness: branches var mı?
SELECT EXISTS (
  SELECT 1 FROM github_branches b
  JOIN repositories r ON r.id = b.repo_id
  WHERE r.full_name = 'Aptora-Gloria/frontend'
  LIMIT 1
) AS branches_ready;
```

```sql
-- 5) Readiness: commits var mı?
SELECT EXISTS (
  SELECT 1 FROM github_repository_commits c
  JOIN repositories r ON r.id = c.repository_id
  WHERE r.full_name = 'Aptora-Gloria/frontend'
  LIMIT 1
) AS commits_ready;
```

```sql
-- 6) Readiness: PR details var mı? (additions not null)
SELECT EXISTS (
  SELECT 1 FROM github_pull_requests pr
  JOIN repositories r ON r.id = pr.repository_id
  WHERE r.full_name = 'Aptora-Gloria/frontend'
    AND pr.additions IS NOT NULL
  LIMIT 1
) AS pr_details_ready;
```

```sql
-- 7) Readiness: reviews var mı? (join)
SELECT EXISTS (
  SELECT 1
  FROM github_pull_request_reviews rv
  JOIN github_pull_requests pr ON pr.id = rv.pull_request_id
  JOIN repositories r ON r.id = pr.repository_id
  WHERE r.full_name = 'Aptora-Gloria/frontend'
  LIMIT 1
) AS reviews_ready;
```

### Curl / Postman doğrulama adımları (en az 3)

```bash
# 1) Sync status
curl -s -X POST 'http://localhost:8080/api/github/repositories/sync/status' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <JWT>' \
  -d '{"fullName":"Aptora-Gloria/frontend"}'
```

```bash
# 2) Analyze
curl -s -X POST 'http://localhost:8080/api/github/repositories/analyze' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <JWT>' \
  -d '{"fullName":"Aptora-Gloria/frontend"}'
```

```bash
# 3) Idempotency smoke: analyze’i 3 kere çağır (aktif job/duplicate oluşmamalı)
for i in 1 2 3; do
  curl -s -X POST 'http://localhost:8080/api/github/repositories/analyze' \
    -H 'Content-Type: application/json' \
    -H 'Authorization: Bearer <JWT>' \
    -d '{"fullName":"Aptora-Gloria/frontend"}' | jq .
done
```

