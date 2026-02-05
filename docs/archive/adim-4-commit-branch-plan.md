# Adım 4: Commit ve Branch – Detaylı Plan

Bu doküman, Metrics modülünün commit ve branch verisi için GitHub JPA/entity kullanımından port’lara geçişini (Adım 4) adım adım planlar.

---

## 1. Mevcut durum

### Commit

| Taraf | Durum |
|-------|--------|
| **GitHub CommitInfo** | `sha`, `nodeId`, `message`, `authorLogin`, `committerLogin`, `parentShas`, `htmlUrl`, `apiUrl`. **Eksik:** `committedAt`, `mergeCommit`. |
| **GetCommitsPort** | `getCommits(login, fullName, branchName)` → `List<CommitInfo>`. **Önemli:** `CommitServiceAdapter` içinde `branchName == null` veya boş iken **tüm repo** commit’leri dönüyor (`findByRepository(repository)`). Yani yeni port gerekmez; `branchName = null` kullanımı yeterli. |
| **Metrics CommitRaw** | `authorLogin`, `committedAt`, `mergeCommit`, `messageLength`. `messageLength` = `message != null ? message.length() : 0` (Metrics’te türetilebilir). |

### Branch

| Taraf | Durum |
|-------|--------|
| **GitHub BranchInfo** | Sadece `name`. **Eksik:** `commitCount`, `lastCommitDate`. |
| **GetBranchesPort** | `getBranches(login, fullName)` → `List<BranchInfo>`. |
| **Metrics BranchRaw** | `branchName`, `commitCount`, `lastCommitDate`, `isDefault`. `isDefault` = `branch.name().equals(repoDefaultBranch)` (Metrics’te `RepositoryInfo.defaultBranch()` ile hesaplanır). |

---

## 2. GitHub tarafı yapılacaklar

### 2.1 CommitInfo genişletme

- **CommitInfo** (application DTO) record’a ekle:
  - `OffsetDateTime committedAt`
  - `Boolean mergeCommit`
- **CommitMapper:**
  - `toCommitInfo(CommitEntity)`: `entity.getCommittedAt()`, `entity.getMergeCommit()` set et.
  - `fromApiDto(CommitDto)`: API’deki `commit.committer().date()` (veya `author().date()`) string’ini parse edip `committedAt` yap; `mergeCommit = parents != null && parents.size() > 1`.
- **Davranış dokümantasyonu:** `GetCommitsPort.getCommits(login, fullName, branchName)` için `branchName == null` veya boş olduğunda **tüm repository** commit’lerinin döndüğü açıkça dokümante edilsin (javadoc / md).

### 2.2 BranchInfo genişletme

- **BranchInfo** record’a ekle:
  - `Long commitCount`
  - `OffsetDateTime lastCommitDate`
- **BranchMapper / BranchQueryPort (CommitServiceAdapter veya BranchServiceAdapter):**
  - Branch listesi dönerken her branch için:
    - `CommitRepository.findByBranch(branch)` (veya benzeri) ile commit sayısı ve `max(committedAt)` hesaplansın.
    - `new BranchInfo(name, commitCount, lastCommitDate)` dönsün.
  - Mevcut `BranchMapper.toBranchInfo(BranchEntity)` tek parametre alıyor; ya overload eklenir (entity + commitCount + lastCommitDate) ya da branch listesi üreten servis (BranchServiceAdapter) bu alanları doldurur ve BranchMapper genişletilir.

---

## 3. Metrics tarafı yapılacaklar

### 3.1 Commit

- **RepositoryMetricsServiceAdapter:**
  - `CommitRepository` ve `CommitEntity` kaldır.
  - `GetCommitsPort` inject et.
  - Repo commit’leri: `getCommitsPort.getCommits(login, repoFullName, null)` → `List<CommitInfo>`.
  - **RepositoryMetricsPersistenceMapper:** `toCommitRaw(CommitInfo)` ekle (authorLogin, committedAt, mergeCommit, messageLength = message != null ? message.length() : 0).
  - Tüm `commitRepository.findByRepository(...)` ve `CommitEntity` kullanan yerler bu port + mapper ile değiştirilir (getCommitMetricsInput, getActivityTrendInput, getActivityTrendInfo, getCodeQualityInfo, getContributorMetricsInput, getContributorMetricsInfo).
- **UserMetricsServiceAdapter:**
  - Commit listesi: user repo’ları için her birinde `getCommits(login, repoFullName, null)` çağrılır; listeler birleştirilir (repo listesi hâlâ Adım 6’da port’a geçecek; şimdilik mevcut repo listesi ile).

### 3.2 Branch

- **RepositoryMetricsServiceAdapter:**
  - `BranchRepository` ve `BranchEntity` kaldır.
  - `GetBranchesPort` zaten var; inject et.
  - Default branch: `getRepositoryInfo(login, repoFullName).defaultBranch()`.
  - Branch listesi: `getBranchesPort.getBranches(login, repoFullName)` → `List<BranchInfo>` (commitCount, lastCommitDate dolu).
  - **RepositoryMetricsPersistenceMapper:** `toBranchRaw(BranchInfo info, String defaultBranch)` ekle → `BranchRaw(info.name(), info.commitCount(), info.lastCommitDate(), info.name().equals(defaultBranch))`.
  - `getBranchMetricsInput` ve `toBranchRaw(BranchEntity, ...)` kaldırılıp yerine port + yeni mapper kullanılır.

### 3.3 Bağımlılık temizliği

- Metrics’ten kaldırılacak: `CommitRepository`, `BranchRepository`, `CommitEntity`, `BranchEntity` import ve kullanımları.
- `getRepositoryEntity` sadece commit/branch path’lerinde kullanılıyorsa ve bu path’ler port’a geçince artık hiç kullanılmıyorsa, `RepositoryPersistenceAdapter` ve `getRepositoryEntity` da kaldırılabilir (önce tüm kullanım noktaları port’a geçirilmeli).

---

## 4. Uygulama sırası (öneri)

1. **GitHub:** CommitInfo’ya `committedAt`, `mergeCommit` ekle; CommitMapper (toCommitInfo, fromApiDto) güncelle; GetCommitsPort javadoc’ta `branchName == null` davranışını yaz.
2. **GitHub:** BranchInfo’ya `commitCount`, `lastCommitDate` ekle; BranchMapper ve branch listesi üreten adapter’ı güncelle.
3. **Metrics:** Mapper’a `toCommitRaw(CommitInfo)` ve `toBranchRaw(BranchInfo, String defaultBranch)` ekle.
4. **Metrics:** RepositoryMetricsServiceAdapter’da commit path’ini GetCommitsPort’a geçir; sonra branch path’ini GetBranchesPort’a geçir.
5. **Metrics:** UserMetricsServiceAdapter’da commit path’ini GetCommitsPort’a geçir (repo listesi aynı kalır, Adım 6’da değişir).
6. **Metrics:** CommitRepository, BranchRepository, CommitEntity, BranchEntity import ve field’larını kaldır.

---

## 5. Risk ve notlar

- **getCommits(login, fullName, null):** Bu davranış GitHub’da sözleşme (contract) olarak sabitlenmeli; başka tüketiciler de kullanabilir.
- **Branch sayısı çok yüksek repolarda:** Her branch için commit sayısı/son tarih hesaplamak ek yük. Gerekirse ileride sayfalama veya lazy/async doldurma düşünülebilir.
- **Null güvenliği:** BranchInfo.commitCount / lastCommitDate null olabilir mi? Branch’te hiç commit yoksa `lastCommitDate = null`, `commitCount = 0` gibi bir kural konulabilir; Metrics mapper’da null-safe kullanım yapılmalı.
