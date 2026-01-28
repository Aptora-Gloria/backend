# Sistem Analizi Özeti

## 1. DB Şema Özeti

### `repositories` Tablosu
```sql
CREATE TABLE repositories (
    id BIGSERIAL PRIMARY KEY,
    github_repo_id BIGINT NOT NULL UNIQUE,  -- UK
    name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,        -- owner/repo
    description TEXT,
    html_url VARCHAR(500),
    is_private BOOLEAN,
    is_fork BOOLEAN,
    stargazers_count INTEGER,
    watchers_count INTEGER,
    forks_count INTEGER,
    open_issues_count INTEGER,
    size INTEGER,                            -- KB
    language VARCHAR(50),
    default_branch VARCHAR(50),
    owner_login VARCHAR(100) NOT NULL,
    owner_type VARCHAR(20),                  -- USER, ORGANIZATION
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    pushed_at TIMESTAMP WITH TIME ZONE,
    last_synced_at TIMESTAMP WITH TIME ZONE,
    system_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    system_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id BIGINT NOT NULL,                 -- FK -> github_users.id
    CONSTRAINT fk_repo_user FOREIGN KEY (user_id) REFERENCES github_users(id)
);
```

**Unique Constraints:**
- `github_repo_id` (UNIQUE)

**Foreign Keys:**
- `user_id` → `github_users.id`

---

### `github_branches` Tablosu
```sql
CREATE TABLE github_branches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    repo_id BIGINT NOT NULL,                 -- FK -> repositories.id
    CONSTRAINT fk_branch_repo FOREIGN KEY (repo_id) REFERENCES repositories(id),
    CONSTRAINT uk_branch_repo_name UNIQUE (repo_id, name)  -- UK
);
```

**Unique Constraints:**
- `(repo_id, name)` composite UNIQUE

**Foreign Keys:**
- `repo_id` → `repositories.id`

**Not:** `last_commit_sha` alanı yok. Commit'ler ayrı tabloda tutuluyor.

---

### `github_repository_commits` Tablosu
```sql
CREATE TABLE github_repository_commits (
    id BIGSERIAL PRIMARY KEY,
    sha VARCHAR(40) NOT NULL,
    node_id VARCHAR(200),
    message TEXT,
    author_name VARCHAR(200),
    author_email VARCHAR(255),
    authored_at TIMESTAMP WITH TIME ZONE,
    committer_name VARCHAR(200),
    committer_email VARCHAR(255),
    committed_at TIMESTAMP WITH TIME ZONE,
    author_login VARCHAR(100),
    committer_login VARCHAR(100),
    is_merge_commit BOOLEAN,
    html_url VARCHAR(500),
    api_url VARCHAR(500),
    repository_id BIGINT NOT NULL,           -- FK -> repositories.id
    branch_id BIGINT NOT NULL,               -- FK -> github_branches.id
    CONSTRAINT fk_commit_repo FOREIGN KEY (repository_id) REFERENCES repositories(id),
    CONSTRAINT fk_commit_branch FOREIGN KEY (branch_id) REFERENCES github_branches(id),
    CONSTRAINT uk_commit_sha_branch UNIQUE (sha, branch_id)  -- UK
);
```

**Unique Constraints:**
- `(sha, branch_id)` composite UNIQUE (aynı commit farklı branch'lerde olabilir)

**Foreign Keys:**
- `repository_id` → `repositories.id`
- `branch_id` → `github_branches.id`

---

### `github_pull_requests` Tablosu
```sql
CREATE TABLE github_pull_requests (
    id BIGSERIAL PRIMARY KEY,
    github_pr_id BIGINT NOT NULL UNIQUE,     -- UK
    pr_number INTEGER NOT NULL,
    repository_id BIGINT NOT NULL,           -- FK -> repositories.id
    data_level VARCHAR(20) NOT NULL,        -- BASIC, FULL
    additions INTEGER,                        -- nullable (PR_DETAIL job sonrası dolar)
    deletions INTEGER,                        -- nullable
    changed_files INTEGER,                    -- nullable
    change_stats_synced_at TIMESTAMP WITH TIME ZONE,
    state VARCHAR(20),                       -- open, closed
    draft BOOLEAN,
    title VARCHAR(500),
    body TEXT,
    author_login VARCHAR(100),
    author_github_id BIGINT,
    html_url VARCHAR(500),
    merge_commit_sha VARCHAR(40),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    merged_at TIMESTAMP WITH TIME ZONE,
    last_synced_at TIMESTAMP WITH TIME ZONE,
    system_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    system_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_pull_request_repository FOREIGN KEY (repository_id) REFERENCES repositories(id)
);
```

**Unique Constraints:**
- `github_pr_id` (UNIQUE)

**Foreign Keys:**
- `repository_id` → `repositories.id`

**Not:** `additions`, `deletions`, `changed_files` nullable çünkü PR_DETAIL job tamamlanana kadar boş kalıyor.

---

### `github_repository_languages` Tablosu
```sql
CREATE TABLE github_repository_languages (
    id BIGSERIAL PRIMARY KEY,
    language_name VARCHAR(100) NOT NULL,
    bytes BIGINT NOT NULL,
    percentage DECIMAL(5,2),
    repository_id BIGINT NOT NULL,           -- FK -> repositories.id
    last_synced_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_language_repo FOREIGN KEY (repository_id) REFERENCES repositories(id)
);
```

**Unique Constraints:**
- **YOK** - Aynı repo'da aynı dil birden fazla kez kaydedilebilir (unique constraint yok)

**Foreign Keys:**
- `repository_id` → `repositories.id`

**Not:** Unique constraint yok, bu potansiyel bir sorun olabilir. Normalde `(repository_id, language_name)` unique olmalı.

---

### `github_pull_request_reviews` Tablosu
```sql
CREATE TABLE github_pull_request_reviews (
    id BIGSERIAL PRIMARY KEY,
    github_review_id BIGINT NOT NULL UNIQUE, -- UK
    pull_request_id BIGINT NOT NULL,         -- FK -> github_pull_requests.id
    reviewer_login VARCHAR(100),
    reviewer_github_id BIGINT,
    state VARCHAR(20),                       -- APPROVED, CHANGES_REQUESTED, COMMENTED
    body TEXT,
    submitted_at TIMESTAMP WITH TIME ZONE,
    html_url VARCHAR(500),
    system_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    system_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_pull_request_review_pull_request FOREIGN KEY (pull_request_id) REFERENCES github_pull_requests(id)
);
```

**Unique Constraints:**
- `github_review_id` (UNIQUE)

**Foreign Keys:**
- `pull_request_id` → `github_pull_requests.id`

---

### `github_pull_request_commits` Tablosu
```sql
CREATE TABLE github_pull_request_commits (
    id BIGSERIAL PRIMARY KEY,
    github_repo_id BIGINT NOT NULL,
    sha VARCHAR(40) NOT NULL,
    pull_request_id BIGINT NOT NULL,         -- FK -> github_pull_requests.id
    message TEXT,
    author_login VARCHAR(100),
    author_name VARCHAR(200),
    author_email VARCHAR(200),
    committed_at TIMESTAMP WITH TIME ZONE,
    html_url VARCHAR(500),
    system_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    system_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_github_pull_request_commits_repo_sha UNIQUE (github_repo_id, sha),  -- UK
    CONSTRAINT fk_pull_request_commit_pull_request FOREIGN KEY (pull_request_id) REFERENCES github_pull_requests(id)
);
```

**Unique Constraints:**
- `(github_repo_id, sha)` composite UNIQUE

**Foreign Keys:**
- `pull_request_id` → `github_pull_requests.id`

---

### `github_sync_jobs` Tablosu
```sql
CREATE TABLE github_sync_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_type VARCHAR(20) NOT NULL,           -- PR_LIST, PR_DETAIL, PR_REVIEWS, PR_COMMITS
    repo_full_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,            -- PENDING, RUNNING, COMPLETED, FAILED
    cursor VARCHAR(500),
    current_page INTEGER,
    attempt_count INTEGER DEFAULT 0,
    error_message TEXT,
    next_run_at TIMESTAMP WITH TIME ZONE,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    system_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    system_updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

**Unique Constraints:**
- Yok

**Foreign Keys:**
- Yok (repo_full_name string olarak tutuluyor, FK yok)

---

### `github_users` Tablosu
```sql
CREATE TABLE github_users (
    id BIGSERIAL PRIMARY KEY,
    github_user_id BIGINT NOT NULL UNIQUE,   -- UK
    login VARCHAR(100) NOT NULL,
    login_ci VARCHAR(100) NOT NULL,          -- case-insensitive
    access_token VARCHAR(500),
    name VARCHAR(200),
    avatar_url VARCHAR(500),
    html_url VARCHAR(500),
    owner_type VARCHAR(20) NOT NULL,         -- USER, ORGANIZATION
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_synced_at TIMESTAMP WITH TIME ZONE
);
```

**Unique Constraints:**
- `github_user_id` (UNIQUE)

**Foreign Keys:**
- Yok

---

## 2. Mevcut API Yüzeyi (Controller Listesi)

### Repository Endpoints
- **POST** `/api/github/repos/init/user` → GitHub'dan user repo'larını çeker ve DB'ye kaydeder (E)
- **POST** `/api/github/repos/query` → DB'den repo listesi döner (H)

### Branch Endpoints
- **POST** `/api/github/branches/init?fullName={fullName}` → GitHub'dan branch'leri çeker ve DB'ye kaydeder (E)
- **GET** `/api/github/branches/query?fullName={fullName}` → DB'den branch listesi döner (H)

### Commit Endpoints
- **POST** `/api/github/commits/init` → GitHub'dan commit'leri çeker ve DB'ye kaydeder (E)
  - Request: `{fullName, branchName, since}` (since: ISO date string)
- **POST** `/api/github/commits/query` → DB'den commit listesi döner (H)
  - Request: `{fullName, branchName}`

### Pull Request Endpoints
- **POST** `/api/github/pull-requests/query` → DB'den PR listesi döner (H)
- **POST** `/api/github/pull-requests/query/single` → DB'den tek PR döner (H)
- **POST** `/api/github/pull-requests/reviews` → DB'den PR review'ları döner (H)
- **POST** `/api/github/pull-requests/commits` → DB'den PR commit'leri döner (H)
- **POST** `/api/github/pull-requests/sync/initiate` → PR sync job'ı başlatır (E - async)
- **POST** `/api/github/pull-requests/sync/status` → Sync durumunu döner (H)

### Language Endpoints
- **POST** `/api/github/languages/init` → GitHub'dan language'leri çeker ve DB'ye kaydeder (E)
- **POST** `/api/github/languages/query` → DB'den language listesi döner (H)
- **POST** `/api/github/languages/query/ui` → UI için formatlanmış language listesi döner (H)

### Metrics Endpoints
- **POST** `/api/metrics/repository` → Tüm repository metriklerini döner
- **POST** `/api/metrics/repository/overview` → Repository genel bakış metrikleri
- **POST** `/api/metrics/repository/pull-requests` → PR metrikleri
- **POST** `/api/metrics/repository/commits` → Commit metrikleri
- **POST** `/api/metrics/repository/reviews` → Review metrikleri
- **POST** `/api/metrics/repository/branches` → Branch metrikleri
- **POST** `/api/metrics/repository/languages` → Language metrikleri
- **POST** `/api/metrics/repository/activity` → Aktivite trend metrikleri (12 haftalık)
- **POST** `/api/metrics/repository/code-quality` → Kod kalitesi metrikleri
- **POST** `/api/metrics/repository/contributors` → Contributor metrikleri
- **POST** `/api/metrics/repository/health` → Repository sağlık skoru

**Not:** Tüm metric endpoint'leri DB'den okur (H), GitHub API'ye gitmez.

---

## 3. GitHub API Client Katmanı

### Client Adapter'lar (WebClient kullanıyor)

#### `GitHubRepoQueryAdapter`
- `getUserRepos(String accessToken)` → `/user/repos` endpoint'ini çağırır

#### `BranchClientAdapter`
- `getRepoBranchNames(String accessToken, String ownerLogin, String repoName)` → `/repos/{owner}/{repo}/branches` (pagination ile)

#### `CommitClientAdapter`
- `getCommits(String accessToken, String owner, String repo, String branch, String since)` → `/repos/{owner}/{repo}/commits?sha={branch}&since={since}`

#### `PullRequestClientAdapter`
- `getPullRequests(String accessToken, String owner, String repo, int page)` → `/repos/{owner}/{repo}/pulls?state=all&per_page=100&page={page}`
- `getPullRequestDetails(String accessToken, String owner, String repo, int prNumber)` → `/repos/{owner}/{repo}/pulls/{number}`
- `getPullRequestReviews(String accessToken, String owner, String repo, int prNumber)` → `/repos/{owner}/{repo}/pulls/{number}/reviews`
- `getPullRequestCommits(String accessToken, String owner, String repo, int prNumber)` → `/repos/{owner}/{repo}/pulls/{number}/commits`

#### `LanguageClientAdapter`
- `getLanguages(String accessToken, String owner, String repo)` → `/repos/{owner}/{repo}/languages`

**Özet:**
- ✅ `fetchRepos` → `getUserRepos`
- ✅ `fetchBranches(fullName)` → `getRepoBranchNames`
- ✅ `fetchCommits(fullName, branch, since)` → `getCommits`
- ✅ `fetchPullRequests(fullName, state, page)` → `getPullRequests`
- ✅ `fetchPullRequestDetail(fullName, number)` → `getPullRequestDetails`
- ✅ `fetchLanguages(fullName)` → `getLanguages`

---

## 4. Data Readiness İhtiyacı (Metric Üretimi için Minimum Set)

### Metric-1: Repository Overview
**Gereken tablolar/alanlar:**
- `repositories`: `full_name`, `name`, `description`, `html_url`, `owner_login`, `owner_type`, `default_branch`, `language`, `stargazers_count`, `forks_count`, `watchers_count`, `open_issues_count`, `is_private`, `is_fork`, `size`, `created_at`, `updated_at`, `pushed_at`

**Minimum gereksinim:** RepositoryEntity kaydı olmalı.

---

### Metric-2: Pull Request Metrics
**Gereken tablolar/alanlar:**
- `github_pull_requests`: `state`, `draft`, `created_at`, `merged_at`, `closed_at`, `additions`, `deletions`, `changed_files`, `author_login`
- `github_pull_request_reviews`: `state`, `submitted_at`, `reviewer_login` (opsiyonel, review metrikleri için)

**Minimum gereksinim:**
- PR_LIST job tamamlanmış olmalı (PR'lar BASIC seviyede kaydedilmiş)
- PR_DETAIL job tamamlanmış olmalı (`additions`, `deletions`, `changed_files` dolu)

**Not:** Eğer PR_DETAIL job tamamlanmamışsa, change statistics metrikleri boş/null döner.

---

### Metric-3: Commit Metrics
**Gereken tablolar/alanlar:**
- `github_repository_commits`: `sha`, `committed_at`, `author_login`, `committer_login`, `is_merge_commit`, `message`
- `github_branches`: `name` (branch bazlı analiz için)

**Minimum gereksinim:**
- Branch init yapılmış olmalı
- Commit init yapılmış olmalı (belirli bir branch ve `since` tarihi ile)

**Not:** Commit'ler branch bazlı tutulduğu için, her branch için ayrı init gerekir.

---

### Metric-4: Review Metrics
**Gereken tablolar/alanlar:**
- `github_pull_request_reviews`: `state`, `submitted_at`, `reviewer_login`, `reviewer_github_id`, `body`
- `github_pull_requests`: `pr_number`, `repository_id` (join için)

**Minimum gereksinim:**
- PR_LIST job tamamlanmış olmalı
- PR_REVIEWS job tamamlanmış olmalı (her PR için)

**Not:** PR_REVIEWS job'ları PR_DETAIL job'ından sonra otomatik schedule edilir.

---

### Metric-5: Language Metrics
**Gereken tablolar/alanlar:**
- `github_repository_languages`: `language_name`, `bytes`, `percentage`

**Minimum gereksinim:**
- Language init yapılmış olmalı (`/api/github/languages/init`)

---

### Metric-6: Activity Trend (12 haftalık)
**Gereken tablolar/alanlar:**
- `github_repository_commits`: `committed_at` (son 12 hafta)
- `github_pull_requests`: `created_at`, `merged_at` (son 12 hafta)
- `github_pull_request_reviews`: `submitted_at` (son 12 hafta)

**Minimum gereksinim:**
- Commit'ler son 12 hafta için sync edilmiş olmalı
- PR'lar sync edilmiş olmalı
- Review'lar sync edilmiş olmalı

---

### Metric-7: Code Quality
**Gereken tablolar/alanlar:**
- `github_pull_requests`: `additions`, `deletions`, `changed_files`, `merged_at`
- `github_pull_request_reviews`: `state` (APPROVED, CHANGES_REQUESTED)

**Minimum gereksinim:**
- PR_DETAIL job tamamlanmış olmalı
- PR_REVIEWS job tamamlanmış olmalı

---

### Metric-8: Contributor Metrics
**Gereken tablolar/alanlar:**
- `github_pull_requests`: `author_login`, `author_github_id`
- `github_repository_commits`: `author_login`, `committer_login`
- `github_pull_request_reviews`: `reviewer_login`

**Minimum gereksinim:**
- PR'lar sync edilmiş olmalı
- Commit'ler sync edilmiş olmalı
- Review'lar sync edilmiş olmalı

---

### Metric-9: Health Score
**Gereken tablolar/alanlar:**
- Tüm yukarıdaki metriklerin kombinasyonu

**Minimum gereksinim:**
- Tüm data sync edilmiş olmalı

---

## 5. Sync Davranışı ve Sınırlar

### GitHub Rate Limit
- **Evet, rate limit'e takılıyor**
- Exponential backoff implementasyonu var (`SyncJobServiceAdapter`)
- Rate limit header'ları kontrol ediliyor
- `next_run_at` ile retry mekanizması var

### Veri Aralığı

#### Pull Requests
- **Full history** - Tüm PR'lar çekiliyor (pagination ile)
- `state=all` parametresi ile açık ve kapalı PR'lar
- Incremental sync: `updated_at > last_synced_at` kontrolü yapılıyor

#### Commits
- **Kullanıcı belirliyor** - `since` parametresi ile
- Frontend'den `since` tarihi gönderilmeli
- Örnek: Son 30 gün için `since=2024-01-01T00:00:00Z`

#### Branches
- **Full list** - Tüm branch'ler çekiliyor (pagination ile)

#### Languages
- **Snapshot** - Tek seferlik çekim, timestamp yok

### Multi-User Senaryosu

**Aynı repo farklı user'lar tarafından bağlanabilir mi?**
- **EVET** - `repositories` tablosunda `user_id` FK var
- Her user kendi repo kaydını oluşturuyor
- Aynı `github_repo_id` farklı `user_id`'lerle kaydedilebilir
- **Sorun:** Unique constraint sadece `github_repo_id` üzerinde, `(github_repo_id, user_id)` üzerinde değil
- Bu durumda aynı repo birden fazla kez aynı user için kaydedilemez, ama farklı user'lar için kaydedilebilir

**Repository Uniqueness Kararı:**
- `github_repo_id` unique (tüm sistemde tek)
- Farklı user'lar aynı repo'yu göremez (çünkü unique constraint)
- **Alternatif yaklaşım:** `(github_repo_id, user_id)` unique olmalı (şu an değil)

---

## 6. Log/Issue Örneği

### Senaryo: "Metric boş geliyor çünkü PR detail job tamamlanmamış"

**Akış:**
1. Kullanıcı `/api/github/pull-requests/sync/initiate` endpoint'ini çağırır
2. PR_LIST job başlar, PR'lar BASIC seviyede kaydedilir (`additions`, `deletions`, `changed_files` = NULL)
3. PR_DETAIL job'ları schedule edilir ama henüz tamamlanmamış
4. Kullanıcı `/api/metrics/repository/pull-requests` endpoint'ini çağırır
5. **Sonuç:** 
   - `avgAdditions`, `avgDeletions`, `avgChangedFiles` = 0
   - `largestPrSize`, `smallestPrSize` = 0
   - `small`, `medium`, `large` PR sayıları = 0
   - Çünkü `prsWithStats` listesi boş (tüm PR'ların `additions` = NULL)

**Çözüm:**
- Sync status kontrol edilmeli: `/api/github/pull-requests/sync/status`
- `data_level = FULL` olan PR'ların sayısı kontrol edilmeli
- Frontend'de "Data is being synced, please wait" mesajı gösterilmeli

---

### Senaryo: "Commit metrikleri boş geliyor çünkü commit init yapılmamış"

**Akış:**
1. Kullanıcı repo'yu init eder (`/api/github/repos/init/user`)
2. Branch'leri init eder (`/api/github/branches/init`)
3. **Commit init yapmaz**
4. Kullanıcı `/api/metrics/repository/commits` endpoint'ini çağırır
5. **Sonuç:**
   - `total = 0`
   - `commitsLast30Days = 0`
   - `commitsLast7Days = 0`
   - Tüm metrikler 0/null

**Çözüm:**
- Frontend'de commit init yapılması gerektiği belirtilmeli
- Veya otomatik commit sync mekanizması eklenmeli

---

### Senaryo: "Language metrikleri boş geliyor"

**Akış:**
1. Kullanıcı repo'yu init eder
2. **Language init yapmaz**
3. Kullanıcı `/api/metrics/repository/languages` endpoint'ini çağırır
4. **Sonuç:** Boş liste veya null

**Çözüm:**
- `/api/github/languages/init` endpoint'i çağrılmalı

---

## Özet Tablo: Endpoint → DB Kaydı İlişkisi

| Endpoint | DB'ye Kaydediyor mu? | Hangi Tablo? | Minimum Gereksinim |
|----------|---------------------|--------------|-------------------|
| `POST /api/github/repos/init/user` | ✅ E | `repositories` | - |
| `POST /api/github/repos/query` | ❌ H | `repositories` | Repo init yapılmış olmalı |
| `POST /api/github/branches/init` | ✅ E | `github_branches` | Repo init yapılmış olmalı |
| `GET /api/github/branches/query` | ❌ H | `github_branches` | Branch init yapılmış olmalı |
| `POST /api/github/commits/init` | ✅ E | `github_repository_commits` | Branch init yapılmış olmalı |
| `POST /api/github/commits/query` | ❌ H | `github_repository_commits` | Commit init yapılmış olmalı |
| `POST /api/github/pull-requests/sync/initiate` | ✅ E (async) | `github_pull_requests` | Repo init yapılmış olmalı |
| `POST /api/github/pull-requests/query` | ❌ H | `github_pull_requests` | PR sync başlatılmış olmalı |
| `POST /api/github/languages/init` | ✅ E | `github_repository_languages` | Repo init yapılmış olmalı |
| `POST /api/github/languages/query` | ❌ H | `github_repository_languages` | Language init yapılmış olmalı |
| `POST /api/metrics/repository/*` | ❌ H | Tüm tablolar | İlgili data sync edilmiş olmalı |

---

## Önemli Notlar

1. **Language Entity Unique Constraint Eksik:** `(repository_id, language_name)` unique olmalı, şu an değil.

2. **Commit Sync Manuel:** Commit'ler için otomatik sync yok, frontend'den `since` parametresi ile manuel init gerekli.

3. **PR Sync Async:** PR sync job-based, hemen tamamlanmaz. Status kontrol edilmeli.

4. **Data Level:** PR'lar `BASIC` ve `FULL` seviyelerinde. `FULL` olmadan change statistics metrikleri boş.

5. **Multi-User Repo:** Aynı repo farklı user'lar için kaydedilebilir ama `github_repo_id` unique olduğu için sistem genelinde tek kayıt olur (farklı user'lar aynı repo'yu göremez).
