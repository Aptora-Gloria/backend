# Optimetric — Sunum Gerçeklik Kontrolü (Reality Check)

> Tüm maddeler backend/frontend **kodu ve endpoint’leri** üzerinden teyit edilmiştir. Eski README/diagramlara güvenilmez.

---

## 1) Sunumda GÖSTERİLECEK Akışlar (Kodla doğrulandı)

- [x] **Login (GitHub OAuth)** — ✅  
  **Kanıt:** Frontend: GitHub OAuth URL oluşturulur, kullanıcı GitHub’a yönlendirilir; callback’te token alınır, `POST /auth/login` ile backend’e gönderilir.  
  Backend: `AuthController` — `POST /auth/login` (AuthRequest → AuthResponse), `GET /auth/me` (Authorization: Bearer → GitHubUserDto).  
  Dosya: `src/main/java/com/optimetric/auth/api/AuthController.java`, `LoginPort` / `GetCurrentUserPort`.

- [x] **Repo listeleme** — ✅  
  **Kanıt:** `RepoController` — `POST /api/github/repos/query` (GetReposRequest → List<RepoDto>), `POST /api/github/repos/init/user` (kullanıcı repolarını init).  
  Dosya: `src/main/java/com/optimetric/github/api/RepoController.java`.

- [x] **Repo seçimi** — ✅  
  **Kanıt:** Repo listesi UI’dan seçilir; repo bazlı tüm API’ler `repoFullName` veya `fullName` ile çağrılır. Branch/commit/PR/language endpoint’leri repo scope’unda.  
  Örn: `BranchController` `fullName`, `CommitController` `CommitInitRequest.fullName()`, `PullRequestController` `GetPullRequestsRequest.repoFullName()`.

- [x] **Dashboard metrikleri** — ✅  
  **Kanıt:**  
  - Kullanıcı metrikleri: `MetricsController` — `POST /api/metrics/user` (GetUserMetricsRequest → UserMetricsDto); alt metrikler: `/pr-merge-rate`, `/pr-size`, `/review-stats`, `/commit-time-analysis`, `/language-diversity`, `/activity-trend`.  
  - Repo metrikleri: `RepositoryMetricsController` — `POST /api/metrics/repository`, `/overview`, `/pull-requests`, `/commits`, `/reviews`, `/branches`, `/languages`, `/activity`, `/code-quality`, `/contributors`, `/health`.  
  Dosyalar: `src/main/java/com/optimetric/metrics/api/MetricsController.java`, `RepositoryMetricsController.java`.

- [x] **Manuel sync / init** — ✅  
  **Kanıt:**  
  - Repo init: `POST /api/github/repos/init/user` (kullanıcı repolarını GitHub’dan çekip DB’ye yazar).  
  - Branch init: `POST /api/github/branches/init?fullName=...` (BranchController).  
  - Commit init: `POST /api/github/commits/init` (CommitInitRequest: fullName, branchName, since).  
  - PR sync: `POST /api/github/pull-requests/sync/initiate` (InitiatePullRequestSyncRequest), `POST /api/github/pull-requests/sync/status` (GetSyncStatusRequest → SyncStatusDto).  
  - Dil verisi: `POST /api/github/languages/init`, `POST /api/github/languages/query`.  
  Dosyalar: `RepoController`, `BranchController`, `CommitController`, `PullRequestController`, `LanguageController`.

---

## 2) Sunumda BİLİNÇLİ OLARAK GÖSTERİLMEYECEKLER

- **Repositories sayfasındaki “Sync Now” butonu:** UI’da görünüyor olabilir; backend’de doğrudan “repos sync now” tek endpoint yok; init user + query kullanılıyor. Butonun tam olarak hangi API’yi tetiklediği frontend koduyla netleştirilmeli. (TODO: doğrula → Repositories sayfası “Sync Now” tam olarak hangi backend çağrısını yapıyor?)
- **Rate limit aşıldığında otomatik retry:** Yok; `GitHubRateLimitException` fırlatılıp API’de 429 benzeri yanıt dönülüyor. Sunumda yoğun sync yapılırsa rate limit görülebilir.
- **Webhook / gerçek zamanlı güncelleme:** Kodda webhook dinleyen endpoint yok; veri “sync/init” ile alınıyor.

---

## 3) Bilinen Riskler (Demo sırasında patlayabilecek)

| Risk | Belirti | Anında toparlama cümlesi |
|------|--------|---------------------------|
| GitHub API rate limit | 403/429, `GitHubRateLimitException` log/response | “GitHub’ın istek limiti demo ortamında aşıldı; birkaç dakika sonra otomatik sıfırlanıyor, o anda sadece mevcut DB verisiyle devam edebiliriz.” |
| Boş repo / yeni kullanıcı | Repo listesi veya metrikler boş | “Bu hesap/repo henüz sync edilmemiş; ‘Sync’ veya ‘Init’ ile bir kez veri çekmemiz yeterli.” |
| Token süresi / geçersiz token | 401 `/auth/me` veya API’lerde 401 | “Oturum süresi dolmuş; tekrar GitHub ile giriş yapalım.” |
| Sync uzun sürüyor | PR sync initiate sonrası status “RUNNING” uzun kalır | “Arka planda GitHub’dan veri çekiliyor; büyük repoda birkaç dakika sürebilir, dashboard’u aynı veriyle gösterebiliriz.” |

---

## 4) Sunumda Kullanılacak Güçlü Kanıtlar

- **Hexagonal / modüler yapı:** Port/Adapter ayrımı — `application.port.in` (GetBranchesPort, GetPullRequestsPort, InitCommitsPort vb.), `application.port.out` (BranchQueryPort, PullRequestClientPort vb.); use case’ler `*UseCase` sınıflarında. Örn: `github.application.usecase`, `github.application.port.in`, `github.infrastructure.persistence.adapter`.
- **GitHub doğal kimlikleri:** Repo: `github_repo_id` (unique); PR: `github_pr_id` (unique); PR commit: `(github_repo_id, sha)` unique; branch: `(repo_id, name)` unique; commit: `(sha, branch_id)` unique. Teyit: `RepositoryEntity`, `PullRequestEntity`, V001 migration, `BranchEntity`, `CommitEntity` unique constraint’leri.
- **Rate limit farkındalığı:** `per_page=100` ve sayfalama kullanımı (`PullRequestClientAdapter`), `GitHubRateLimitException` ve `GlobalExceptionHandler` ile anlamlı hata dönülmesi.
- **Metrik modülü ayrımı:** Metrikler ayrı paketlerde — `metrics.api`, `metrics.application.port.in`, `metrics.domain.service`; GitHub verisi `github` modülü port’ları üzerinden okunuyor (repo/PR/commit/branch verisi).

---

## 5) TODO — Sunumdan önce doğrulanması gerekenler (en fazla 10 madde)

1. TODO: doğrula → Frontend’de “Sync Now” (repositories sayfası) tam olarak hangi backend endpoint(ler)ini çağırıyor?
2. TODO: doğrula → `/auth/callback` sonrası frontend’in `POST /auth/login`’e gönderdiği body (code vs. token) formatı ve backend’in beklediği AuthRequest alanları aynı mı?
3. TODO: doğrula → Dashboard sayfasının ilk yüklemede hangi sırayla hangi metrik endpoint’lerini çağırdığı (tek istek mi, çoklu istek mi).
4. TODO: doğrula → Repository “sync status” (hazır mı değil mi) dashboard’a girmeden önce `POST /api/github/repositories/sync/status` ile kontrol ediliyor mu?
5. TODO: doğrula → Demo için kullanılacak GitHub hesabında yeterli repo ve (isteğe bağlı) PR/commit verisi var mı?
6. TODO: doğrula → CORS ve backend base URL’in frontend (localhost:5173) ile uyumlu olduğu (application.yml / env) teyit edilsin.
7. TODO: doğrula → JWT/Authorization header’ın tüm repo/metrics çağrılarında frontend tarafından gönderildiği.
8. En fazla 10 madde kuralına uyuldu.
