# Metrics ↔ GitHub Modül Haberleşmesi

Bu doküman, Metrics modülünün GitHub modülüyle **sadece interface (port) üzerinden** haberleşmesi için mevcut durumu ve refactor planını açıklar.

---

## 1. Mevcut Durum (Sorun)

Metrics modülü şu anda GitHub modülüne **doğrudan infrastructure katmanı üzerinden** bağımlı:


| Metrics kullandığı                                                                                           | GitHub'daki konum                  | Sorun                                                                  |
| ------------------------------------------------------------------------------------------------------------ | ---------------------------------- | ---------------------------------------------------------------------- |
| `GetRepositoryForCurrentUserUseCase`                                                                         | application/usecase                | ✅ Port/use case – doğru                                                |
| `RepositoryPersistenceAdapter`                                                                               | infrastructure/persistence/adapter | ❌ Adapter ve `findEntityById`, `findEntityByFullName` – entity dönüyor |
| `PullRequestRepository` (JPA)                                                                                | infrastructure/persistence/jpa     | ❌ Doğrudan JPA repository                                              |
| `PullRequestReviewRepository` (JPA)                                                                          | infrastructure/persistence/jpa     | ❌ Doğrudan JPA repository                                              |
| ~~`CommitRepository`~~, ~~`BranchRepository`~~, ~~`LanguageRepository`~~, ~~`RepositoryPersistenceAdapter`~~ | –                                  | ✅ Adım 1–4’te repo metrikleri tarafında kaldırıldı                     |
| ~~`RepositoryRepository`~~, ~~`PullRequestRepository`~~, ~~`PullRequestReviewRepository`~~                   | –                                  | ✅ Adım 6'da UserMetricsServiceAdapter port'lara geçirildi              |
| ~~Entity'ler~~                                                                                               | –                                  | ✅ Metrics artık sadece application DTO kullanıyor                      |


Sonuç:

- Metrics, GitHub’ın **application port’ları** yerine **infrastructure** (JPA repo’lar, entity’ler, persistence adapter) kullanıyor.
- Modüller arası sınır ihlal ediliyor; Metrics, GitHub’ın iç implementasyonuna (veritabanı şeması, entity’ler) bağlı.
- Aynı veri hem GitHub (sync/query) hem Metrics (metrik hesaplama) tarafından kullanılıyor ama **interface üzerinden değil**, doğrudan aynı tablolara erişimle – tekrar (duplication) ve kopukluk hissi buradan geliyor.

## **Not:** Adım 1–6 ile bu bağımlılıklar kaldırıldı; Metrics artık sadece port ve DTO kullanıyor.

## 2. Hedef: Sadece Port Üzerinden Haberleşme

Metrics modülü GitHub’a **sadece** şunlar üzerinden erişmeli:

- **GitHub application port’ları (inbound):** Use case’lerin implement ettiği interface’ler (`GetPullRequestsPort`, `GetCommitsPort`, vb.)
- **GitHub application DTO’ları:** `RepositoryInfo`, `PullRequestInfo`, `CommitInfo`, `ReviewInfo`, `BranchInfo`, `LanguageInfo`

Metrics tarafında **olmaması gerekenler:**

- `com.optimetric.github.infrastructure.*` import’ları (entity, jpa, adapter)
- `RepositoryEntity`, `PullRequestEntity`, `CommitEntity` vb. kullanımı

---

## 3. GitHub Modülünde Zaten Var Olan Port’lar


| Port (application/port/in)                     | Dönüş tipi                     | Metrics’in ihtiyacı                                                                   |
| ---------------------------------------------- | ------------------------------ | ------------------------------------------------------------------------------------- |
| `GetRepositoryForCurrentUserUseCase` (execute) | `Optional<RepositoryInfo>`     | Repo erişim + overview alanları – yeterli                                             |
| `GetPullRequestsPort`                          | `List<PullRequestInfo>`        | PR metrikleri – kullanılabilir                                                        |
| `GetPullRequestReviewsPort`                    | `List<ReviewInfo>` (PR başına) | Tüm repo için N+1 veya toplu port gerekebilir                                         |
| `GetCommitsPort`                               | `List<CommitInfo>`             | ✅ `branchName=null` ile repo geneli; `CommitInfo` içinde `committedAt`, `mergeCommit` |
| `GetBranchesPort`                              | `List<BranchInfo>`             | ✅ `commitCount`, `lastCommitDate` eklendi; Metrics kullanıyor                         |
| `GetLanguagesPort`                             | `LanguageInfo` (Map)           | Dil metrikleri – kullanılabilir                                                       |


---

## 4. Eksik / Genişletilmesi Gerekenler (GitHub Tarafı)

Metrics’in port’larla tek taraflı çalışması için GitHub’da aşağıdakiler gerekir:

1. **Commit verisi** ✅ (Adım 4’te yapıldı)
  - `CommitInfo` içinde `committedAt`, `mergeCommit` eklendi; repo geneli için `GetCommitsPort.getCommits(login, repoFullName, null)` kullanılıyor. Metrics artık sadece bu port üzerinden commit verisi alıyor.
2. **Branch verisi** ✅ (Adım 4'te yapıldı)
  - `BranchInfo` genişletildi: `commitCount`, `lastCommitDate` eklendi. `GetBranchesPort` ile branch listesi alınıyor; Metrics branch verisi için sadece bu port ve DTO kullanıyor.
3. **Review’lar (isteğe bağlı)**
  - Şu an: `GetPullRequestReviewsPort.getPullRequestReviews(login, repoFullName, prNumber)` – PR başına.
  - Repo bazlı tek çağrı istenirse: örn. `getAllReviewsForRepository(login, repoFullName) → List<ReviewInfo>` gibi bir port eklenebilir (N+1’i GitHub içinde çözer).

---

## 5. Refactor Adımları (Özet)

### Adım 1: Metrics’te “repo kimliği” ve overview için sadece application kullan

- `getRepositoryEntity(login, repoFullName)` yerine:
  - `GetRepositoryForCurrentUserUseCase.executeOrThrow(login, repoFullName)` → `RepositoryInfo`.
- Overview için tüm alanlar `RepositoryInfo`’dan doldurulsun; **RepositoryEntity**, **RepositoryPersistenceAdapter**, `findEntityById`, `findEntityByFullName` Metrics’ten kaldırılsın.
- Böylece Metrics, repo erişimi ve overview için **sadece** GitHub application (use case + DTO) kullanır. Overview path'i Adım 1'de güncellendi: `getRepositoryInfo` + `buildOverviewInfo(RepositoryInfo)` kullanılıyor.

### Adım 2: PR verisi için sadece GetPullRequestsPort ✅ (Yapıldı)

- `PullRequestRepository` ve `PullRequestReviewRepository` kaldırıldı; `GetPullRequestsPort` ve `GetPullRequestReviewsPort` kullanılıyor.
- `GetPullRequestsPort.getPullRequestsForRepository(login, repoFullName)` → `List<PullRequestInfo>`.
- Metrics tarafında `PullRequestInfo` → `PrRaw` (ve gerekliyse domain input) mapping’i yapılsın.
- `PullRequestInfo` içinde `changeStats` (additions, deletions, changedFiles) zaten var; gerekli alanlar kontrol edilip mapping tamamlansın.

### Adım 3: Dil verisi için sadece GetLanguagesPort ✅ (Yapıldı)

- `LanguageRepository` ve `LanguageEntity` Metrics'ten kaldırıldı; `GetLanguagesPort` ve `toLanguageRawList(LanguageInfo)` kullanılıyor.
- `GetLanguagesPort.getLanguages(repoFullName)` → `LanguageInfo` (Map).
- Bu map’ten `List<LanguageRaw>` (veya mevcut domain input) üretilsin.

### Adım 4: Commit ve branch ✅ (Yapıldı)

- GitHub'da: `CommitInfo`'a `committedAt`, `mergeCommit` eklendi; `BranchInfo`'a `commitCount`, `lastCommitDate` eklendi. Repo geneli commit için `GetCommitsPort.getCommits(login, repoFullName, null)` kullanılıyor.
- Metrics'te: `CommitRepository`, `BranchRepository` ve ilgili entity kullanımları kaldırıldı; sadece `GetCommitsPort`, `GetBranchesPort` ve DTO'lar kullanılıyor.

### Adım 5: Review'lar ✅ (Seçenek A – port üzerinden)

- Mevcut `GetPullRequestReviewsPort` kullanılıyor; Metrics (hem repo hem user) tüm PR'lar için döngüde çağırıyor (N+1, port üzerinden).
- İsteğe bağlı: GitHub'da "tüm review'lar için repo" port'u eklenebilir (Seçenek B); Metrics tek çağrıyla alır.

### Adım 6: UserMetricsServiceAdapter ✅ (Yapıldı)

- `QueryRepositoriesPort.execute(login, null, null)` ile user'ın repoları `List<RepositoryInfo>` alınıyor.
- PR verisi: `GetPullRequestsPort`; review verisi: `GetPullRequestReviewsPort` (PR başına).
- `RepositoryRepository`, `PullRequestRepository`, `PullRequestReviewRepository` ve tüm GitHub entity bağımlılıkları Metrics'ten kaldırıldı.

---

## 6. Bağımlılık Özeti (Hedef)


| Metrics’in kullanacağı                                 | Nerede tanımlı             | Amaç                                  |
| ------------------------------------------------------ | -------------------------- | ------------------------------------- |
| `GetRepositoryForCurrentUserUseCase`                   | github.application.usecase | Repo erişim + RepositoryInfo          |
| `QueryRepositoriesPort`                                | github.application.port.in | User'ın repo listesi → List           |
| `GetPullRequestsPort`                                  | github.application.port.in | PR listesi → PrRaw                    |
| `GetPullRequestReviewsPort`                            | github.application.port.in | Review listesi (PR başına veya toplu) |
| `GetCommitsPort` veya yeni “repo commits” port’u       | github.application.port.in | Commit listesi → CommitRaw            |
| `GetBranchesPort` (veya genişletilmiş branch port’u)   | github.application.port.in | Branch listesi → BranchRaw            |
| `GetLanguagesPort`                                     | github.application.port.in | Dil verisi → LanguageRaw              |
| `RepositoryInfo`, `PullRequestInfo`, `CommitInfo`, vb. | github.application.dto     | Sadece DTO; entity yok                |


Metrics’te **kaldırılacak** bağımlılıklar:

- ~~`RepositoryPersistenceAdapter`~~, ~~`CommitRepository`~~, ~~`BranchRepository`~~, ~~`LanguageRepository`~~, ~~`PullRequestRepository`~~, ~~`PullRequestReviewRepository`~~ (repo metrikleri – Adım 1–4’te kaldırıldı)
- ~~`RepositoryRepository`~~, ~~JPA repo'lar~~, ~~entity'ler~~ (UserMetricsServiceAdapter – Adım 6'da kaldırıldı)

---

## 7. Sonuç

- **Refactor tamamlandı:** Metrics modülü GitHub ile **sadece application port'ları ve DTO'ları** üzerinden haberleşiyor; JPA repository ve entity bağımlılıkları kaldırıldı (Adım 1–6).
- Repo metrikleri: `GetRepositoryForCurrentUserUseCase`, `GetPullRequestsPort`, `GetPullRequestReviewsPort`, `GetCommitsPort`, `GetBranchesPort`, `GetLanguagesPort`.
- User metrikleri: `QueryRepositoriesPort` (repo listesi), aynı PR/Review/Commit/Language port'ları.
- Haberleşme **interfaceler üzerinden**; tekrar (duplication) azaltıldı.

