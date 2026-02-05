# Demo Akışı (Kullanıcı Yolculuğu)

> Her adım backend/frontend **gerçek endpoint ve akışlarına** göre yazılmıştır.

---

## Adım 0 — Uygulamaya giriş

- **Kullanıcının gördüğü:** Ana sayfa; giriş yapmamışsa login’e yönlendirme veya repo/dashboard erişimi engelli.
- **Frontend dosyası/route:** Ana route `/` (LoginPage); giriş sonrası `/repositories` veya `/dashboard` gibi hedefler. (Frontend sibling projede: `App.tsx` route tanımları.)
- **Backend çağrıları:** Doğrudan değil; korumalı sayfalara gidince `Authorization: Bearer` gerekir, yoksa 401.
- **DB etkisi:** Yok.
- **Söylenecek 1 cümle:** “Uygulama GitHub ile giriş istiyor; giriş yapınca repolarınız ve metrikler erişilebilir olacak.”

**Bu adımın amacı:** Kullanıcıyı GitHub OAuth akışına hazırlamak.  
**Teknik detaya girilirse:** Koruma `SecurityConfig` ve `@AuthenticationPrincipal` ile; `/auth/login` ve `/auth/me` permitAll, diğer API’ler authenticated.

---

## Adım 1 — Login (OAuth)

- **Kullanıcının gördüğü:** “GitHub ile giriş” butonu → GitHub consent sayfası → izin ver → uygulamaya geri dönüş (callback).
- **Frontend dosyası/route:** Login sayfası, callback route (örn. `/auth/callback`). OAuth URL oluşturma: frontend’de `buildGitHubAuthUrl` benzeri bir modül. Callback’te code/token alınıp backend’e iletilir.
- **Backend çağrıları:**  
  - `POST /auth/login` — Body: `AuthRequest` (code veya token; kodda teyit edin). Response: `AuthResponse` (JWT vb.).  
  - Sonrasında kullanıcı bilgisi için: `GET /auth/me` — Header: `Authorization: Bearer <token>`. Response: `GitHubUserDto`.
- **DB etkisi:** Login use case kullanıcıyı DB’de yoksa oluşturur / günceller (auth modülü persistence).
- **Söylenecek 1 cümle:** “GitHub hesabınızla giriş yaptınız; artık repolarınız ve metrikler sizin için hazırlanabilir.”

**Bu adımın amacı:** Kimlik doğrulama ve oturum token’ı almak.  
**Teknik detaya girilirse:** `AuthController` → `LoginPort` / `GetCurrentUserPort`; token sonrası tüm isteklerde `Authorization: Bearer` gönderilir.

---

## Adım 2 — Repo seçimi

- **Kullanıcının gördüğü:** Repo listesi (repositories sayfası); bir repo seçilir (detay veya dashboard’a geçiş).
- **Frontend dosyası/route:** Repositories sayfası (örn. `/repositories`), repo detay (örn. `/repositories/:repoId`). (Frontend sibling projede ilgili page component’leri.)
- **Backend çağrıları:**  
  - Repo listesi: `POST /api/github/repos/query` — Body: `GetReposRequest` (ownerType, ownerLogin vb.). Response: `List<RepoDto>`.  
  - İlk kez listeyi doldurmak: `POST /api/github/repos/init/user` — (auth: login). Response: `List<RepoInitRequest>`.
- **DB etkisi:** `init/user` GitHub’dan repoları çekip `repositories` tablosuna yazar; `github_repo_id` unique ile tekrar yazmada güncelleme.
- **Söylenecek 1 cümle:** “Bu liste GitHub’daki repolarınızdan geliyor; birini seçerek metriklerini ve PR/branch verilerini göreceğiz.”

**Bu adımın amacı:** Hangi repo üzerinde çalışılacağını seçmek ve gerekirse veriyi ilk kez sync etmek.  
**Teknik detaya girilirse:** `RepoController` → `QueryRepositoriesPort` / `InitializeUserRepositoriesPort`; repo kimliği `fullName` (owner/repo) ile kullanılır.

---

## Adım 3 — Dashboard

- **Kullanıcının gördüğü:** Kullanıcı veya repo bazlı metrikler: PR merge oranı, PR boyutu, review istatistikleri, commit zaman analizi, dil çeşitliliği, aktivite trendi; repo ise overview, PR, commit, review, branch, dil, aktivite, kod kalitesi, katkıda bulunanlar, sağlık skoru.
- **Frontend dosyası/route:** Dashboard sayfası (örn. `/dashboard`), kullanıcı performansı (örn. `/user-performance`). (Frontend’de bu sayfaların hangi endpoint’leri çağırdığı koddan teyit edilmeli.)
- **Backend çağrıları:**  
  - Kullanıcı metrikleri: `POST /api/metrics/user` — Body: `GetUserMetricsRequest` (login vb.). Response: `UserMetricsDto`. Alt metrikler: `POST /api/metrics/pr-merge-rate`, `/pr-size`, `/review-stats`, `/commit-time-analysis`, `/language-diversity`, `/activity-trend` (aynı request tipi).  
  - Repo metrikleri: `POST /api/metrics/repository` (veya `/overview`, `/pull-requests`, `/commits`, `/reviews`, `/branches`, `/languages`, `/activity`, `/code-quality`, `/contributors`, `/health`) — Body: `GetRepositoryMetricsRequest`. Response: ilgili DTO.
- **DB etkisi:** Sadece okuma; metrics modülü github modülünün verilerini (repo, PR, commit, branch, language) kullanır.
- **Söylenecek 1 cümle:** “Tüm bu metrikler sizin ve reponuzun GitHub verisinden hesaplanıyor; PR’lar, commit’ler ve review’lar tek kaynaktan geliyor.”

**Bu adımın amacı:** Ölçülebilir değerleri göstermek ve “veri → metrik” hattını göstermek.  
**Teknik detaya girilirse:** `MetricsController` / `RepositoryMetricsController` → ilgili Port’lar ve `metrics.domain.service` hesaplayıcıları.

---

## Adım 4 — (Varsa) Manuel Analyze / Sync

- **Kullanıcının gördüğü:** Repo detay veya dashboard’da “Sync Data”, “Init branches”, “Sync PR” vb. butonlar; tıklanınca veri yenilenir.
- **Frontend dosyası/route:** Repo detay sayfası (“Sync Data”), PR sync butonu; dashboard’da force sync parametreli çağrılar. (Repositories sayfası “Sync Now” için REALITY_CHECK’teki TODO geçerli.)
- **Backend çağrıları:**  
  - Branch: `POST /api/github/branches/init?fullName=owner/repo`.  
  - Commit: `POST /api/github/commits/init` — Body: fullName, branchName, since.  
  - PR: `POST /api/github/pull-requests/sync/initiate` — Body: repo fullName vb. Status: `POST /api/github/pull-requests/sync/status`.  
  - Diller: `POST /api/github/languages/init` (repo bilgisi ile).
- **DB etkisi:** GitHub’dan veri çekilir; branch’ler `github_branches` (repo_id, name unique), commit’ler `github_repository_commits` (sha, branch_id unique), PR’lar `github_pull_requests` (github_pr_id unique), diller `github_repository_languages` (repository_id, language_name unique).
- **Söylenecek 1 cümle:** “Bu butonla GitHub’dan güncel veriyi çekiyoruz; büyük repolarda birkaç dakika sürebilir.”

**Bu adımın amacı:** Veriyi güncellemek ve “tetikleme → sync → metrik” bütünlüğünü göstermek.  
**Teknik detaya girilirse:** İlgili Controller’lar → Init* / Initiate* Port’ları → GitHub client adapter’ları ve persistence adapter’ları; idempotency doğal anahtarlarla (github_pr_id, sha, repo_id+name vb.) sağlanır.
