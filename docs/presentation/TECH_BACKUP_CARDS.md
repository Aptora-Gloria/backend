# Soru Gelirse Açılacak Teknik Kartlar

> Her kart 6–10 satır; sunumda hızlı cevap için. Tüm iddialar kod/konfigürasyonla teyit edilmiştir.

---

## Kart: Neden Hexagonal / Modular Monolith?

- **1 cümlelik cevap:** Uygulama mantığı port’lar (arayüzler) üzerinden konuşuyor; altyapı (DB, GitHub API) adapter’larla değiştirilebilir, testte mock’lanabilir ve modüller (auth, github, metrics) paket sınırlarıyla ayrılıyor.
- **2 kanıt:** `com.optimetric.github.application.port.in` (GetBranchesPort, GetPullRequestsPort, InitCommitsPort vb.); `com.optimetric.github.infrastructure.persistence.adapter` (BranchServiceAdapter, PullRequestServiceAdapter) ve `infrastructure.http.adapter` (PullRequestClientAdapter). Aynı pattern auth ve metrics modüllerinde de var.
- **1 trade-off:** Daha fazla sınıf ve katman; basit CRUD için bile port + use case + adapter zinciri. Karşılığı: değişikliklerin yeri net, test ve gelecekteki bölme (örn. repo’yu servis yapma) kolay.

---

## Kart: GitHub API rate limit / pagination nasıl ele alındı?

- **1 cümlelik cevap:** İsteklerde `per_page=100` ve `page` ile sayfalama kullanılıyor; limit aşılırsa anlamlı bir hata fırlatılıp API yanıtında (429 benzeri) dönülüyor, otomatik retry yok.
- **Kanıt:** `PullRequestClientAdapter` içinde `queryParam("per_page", 100)` ve sayfa döngüsü; `GitHubRateLimitException` (reset zamanı ve kalan istek bilgisiyle); `GlobalExceptionHandler.handleGitHubRateLimitException`. Branch tarafında `GitHubConstants` ile branch listesi URL’inde `per_page`/page kullanımı.
- **Sunumda söylenecek:** “GitHub’ın saatlik limiti var; aşıldığında kullanıcıya ne zaman sıfırlanacağını gösteriyoruz, istekleri sayfalayarak limiti idare ediyoruz.”

---

## Kart: Idempotency ve duplicate veri nasıl önleniyor?

- **1 cümlelik cevap:** GitHub’ın doğal kimlikleri (repo id, PR id, commit sha, repo+branch name) unique constraint ve “varsa güncelle / yoksa ekle” mantığıyla kullanılıyor; aynı sync tekrar çalırsa duplicate satır oluşmuyor.
- **Kanıt:** DB: `repositories.github_repo_id` unique; `github_pull_requests.github_pr_id` unique; `github_pull_request_commits` (github_repo_id, sha) unique; `github_branches` (repo_id, name) unique; `github_repository_commits` (sha, branch_id) unique; `github_repository_languages` (repository_id, language_name) unique (V003). Persistence adapter’lar bu alanlara göre lookup/merge yapıyor.
- **Sunumda söylenecek:** “Aynı PR veya commit tekrar gelse bile veritabanında tek kayıt tutuyoruz; GitHub ID ve SHA ile tekilleştiriyoruz.”

---

## Kart: Neden DB id yerine GitHub doğal kimlikleri (fullName / sha / github_pr_id)?

- **1 cümlelik cevap:** Veri kaynağı GitHub; aynı repo/PR/commit’i tekrar çektiğimizde eşleştirmek ve duplicate’ı önlemek için GitHub’ın kendi ID’leri ve sha kullanılıyor; fullName ise API çağrılarında insan okunur tanımlayıcı.
- **Kanıt:** Entity’lerde `githubRepoId`, `githubPrId`, `sha`, `fullName`; unique constraint’ler `github_repo_id`, `github_pr_id`, `(github_repo_id, sha)`, `(sha, branch_id)`, `(repo_id, name)`. Request DTO’larda `repoFullName`, `fullName`, `sha` kullanımı.
- **Trade-off:** GitHub’a bağımlılık; GitHub ID değişirse (pratikte nadir) senkronizasyon mantığı gözden geçirilmeli.

---

## Kart: Auth akışı (token nerede, API’ler nasıl korunuyor)?

- **1 cümlelik cevap:** Frontend GitHub OAuth’tan code/token alıp `POST /auth/login` ile backend’e veriyor; backend JWT (veya benzeri) dönüyor; sonraki tüm isteklerde `Authorization: Bearer <token>` ile gönderiliyor; korumalı endpoint’ler `@AuthenticationPrincipal` ile login/identity alıyor.
- **Kanıt:** `AuthController`: `POST /auth/login` (AuthRequest → AuthResponse), `GET /auth/me` (Bearer token → GitHubUserDto). SecurityConfig: `/auth/login`, `/auth/me` permitAll; diğer API’ler authenticated. Repo/Metrics controller’lar `@AuthenticationPrincipal String login` kullanıyor.
- **Sunumda söylenecek:** “Giriş tamamen GitHub OAuth; aldığımız token’ı saklayıp her istekte gönderiyoruz, böylece veri sadece o kullanıcının repolarına erişiyor.”

---

## Kart: Metrics verisi nereden geliyor (modül sınırı)?

- **1 cümlelik cevap:** Metrikler kendi modülünde hesaplanıyor ama ham veri GitHub modülünden geliyor; metrics, repo/PR/commit/branch/language verisini port’lar üzerinden okuyor, kendi domain hesaplarını yapıp DTO dönüyor.
- **Kanıt:** `metrics.api.MetricsController` / `RepositoryMetricsController` → `metrics.application.port.in` (GetPrMergeRatePort, GetReviewStatsPort vb.) → use case’ler; use case’ler `port.out` ile veri çekiyor. GitHub tarafında `github.application.port.out` (BranchQueryPort, PullRequestQueryPort, CommitClientPort vb.) ve persistence adapter’lar; metrics tarafı bu veriyi “okuyucu” olarak kullanıyor, yazmıyor.
- **Sunumda söylenecek:** “Metrik hesapları bizde; veri kaynağı tek: GitHub modülünün sakladığı repo, PR ve commit verisi.”

---

## Kart: Sync “init” ile “query” farkı ne?

- **1 cümlelik cevap:** “Init” GitHub API’den veri çekip DB’ye yazan endpoint’ler (init user repos, init branches, init commits, PR sync initiate, languages init); “query” ise sadece DB’den okuyup dönen endpoint’ler (repos query, branches query, commits query, PR query, languages query).
- **Kanıt:** Repo: `POST /api/github/repos/init/user` vs `POST /api/github/repos/query`. Branch: `POST /api/github/branches/init` vs `GET /api/github/branches/query`. Commit: `POST /api/github/commits/init` vs `POST /api/github/commits/query`. PR: `POST .../sync/initiate` ve `.../sync/status` vs `POST .../query`, `.../query/single`. Language: `POST /api/github/languages/init` vs `.../languages/query` ve `.../languages/query/ui`.
- **Sunumda söylenecek:** “Önce ‘init’ veya ‘sync’ ile veriyi çekiyoruz, sonra ‘query’ ile hep DB’den hızlıca okuyoruz.”

---

## Kart: PR sync “initiate” ile “status” ne işe yarıyor?

- **1 cümlelik cevap:** “Initiate” arka planda bir sync job başlatır (GitHub’dan PR/review/commit çeker); “status” ise o job’ın durumunu (RUNNING, COMPLETED, FAILED vb.) ve gerekirse ilerleme bilgisini döner; UI böylece “sync devam ediyor” diye gösterebilir.
- **Kanıt:** `PullRequestController`: `POST /api/github/pull-requests/sync/initiate` (InitiatePullRequestSyncRequest), `POST /api/github/pull-requests/sync/status` (GetSyncStatusRequest → SyncStatusDto). Use case: `InitiatePullRequestSyncPort`, `GetSyncStatusPort`; arka planda job ve durum takibi.
- **Sunumda söylenecek:** “Sync’i tetikliyoruz, ardından durum endpoint’iyle bitene kadar takip edebiliyoruz.”
