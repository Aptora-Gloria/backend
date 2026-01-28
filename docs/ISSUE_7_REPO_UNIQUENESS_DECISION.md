# ISSUE 7 — Repo Uniqueness Kararı

## Problem

Repository'lerin aynı sayılması için hangi key'in kullanılacağına karar verilmesi gerekiyor. Bu karar, repository init/upsert akışlarında ve rename senaryolarında kritik öneme sahip.

## Mevcut Durum

Şu anda `RepositoryPersistenceAdapter.saveAll()` metodunda deduplication için `htmlUrl` kullanılıyor:

```java
// Get incoming URLs for deduplication
List<String> incomingUrls = repos.stream()
        .map(RepositoryInitData::htmlUrl)
        .toList();

// Find existing repositories by HTML URLs
List<RepositoryEntity> existingEntities = repositoryRepository.findAllByHtmlUrlIn(incomingUrls);
```

Ancak `RepositoryEntity` içinde:
- `githubRepoId` (Long) - GitHub API'den gelen unique ID
- `fullName` (String) - "owner/repo" formatı
- `htmlUrl` (String) - Repository'nin HTML URL'i

## Karar Seçenekleri

### Seçenek 1: `githubRepoId` (ÖNERİLEN) ⭐

**Avantajlar:**
- GitHub tarafından garanti edilen global unique identifier
- Repository rename durumunda değişmez
- Repository transfer durumunda değişmez
- Numeric olduğu için performans açısından verimli (indexing)
- Database primary key olarak kullanılabilir

**Dezavantajlar:**
- GitHub API'den gelmeyen repository'ler için kullanılamaz (nadir durum)

**Rename Senaryosu:**
- Repository adı değişse bile `githubRepoId` aynı kalır
- `fullName` ve `htmlUrl` güncellenir, ancak repository aynı entity olarak kalır

**Transfer Senaryosu:**
- Repository başka bir organizasyona transfer edilse bile `githubRepoId` aynı kalır
- `ownerLogin` ve `fullName` güncellenir

### Seçenek 2: `fullName` (Alternatif)

**Avantajlar:**
- Human-readable
- "owner/repo" formatı standart GitHub convention
- Kolay anlaşılır

**Dezavantajlar:**
- Repository rename durumunda değişir (yeni repository olarak algılanır)
- Repository transfer durumunda değişir
- Case sensitivity sorunları olabilir
- String comparison performansı numeric'ten daha düşük

**Rename Senaryosu:**
- Repository adı değişirse yeni bir repository olarak algılanır
- Eski repository kaydı silinmez, yeni kayıt oluşturulur
- Veri tutarsızlığı riski

### Seçenek 3: `htmlUrl` (Son Çare)

**Avantajlar:**
- Şu anda kullanılıyor
- Unique identifier olarak çalışıyor

**Dezavantajlar:**
- Repository rename durumunda değişir
- Repository transfer durumunda değişir
- URL formatı değişebilir (GitHub API değişiklikleri)
- String comparison performansı

**Rename Senaryosu:**
- Repository adı değişirse URL değişir, yeni repository olarak algılanır

## Önerilen Karar: `githubRepoId` ⭐

### Gerekçe

1. **GitHub'un Resmi Identifier'ı**: `githubRepoId` GitHub tarafından garanti edilen global unique identifier'dır. Bu, repository'nin gerçek kimliğidir.

2. **Rename/Transfer Dayanıklılığı**: Repository adı değişse veya transfer edilse bile `githubRepoId` aynı kalır. Bu, veri tutarlılığını sağlar.

3. **Performans**: Numeric identifier'lar string comparison'dan daha hızlıdır ve database indexing için daha verimlidir.

4. **Database Design**: `githubRepoId` zaten `@Column(unique = true)` olarak işaretlenmiş ve unique constraint'e sahip.

### Uygulama Planı

1. **RepositoryPersistencePort.saveAll() Güncelleme:**
   - Deduplication logic'i `htmlUrl` yerine `githubRepoId` kullanacak şekilde güncellenir
   - Mevcut repository'ler `githubRepoId` ile kontrol edilir
   - Eğer `githubRepoId` mevcut ise, repository update edilir (upsert)
   - Eğer `githubRepoId` yok ise, yeni repository oluşturulur

2. **Rename Senaryosu:**
   - Repository rename edildiğinde:
     - `githubRepoId` aynı kalır (repository aynı kalır)
     - `fullName` güncellenir
     - `htmlUrl` güncellenir
     - `name` güncellenir
     - Diğer metadata'lar güncellenir

3. **Transfer Senaryosu:**
   - Repository transfer edildiğinde:
     - `githubRepoId` aynı kalır (repository aynı kalır)
     - `ownerLogin` güncellenir
     - `fullName` güncellenir
     - `htmlUrl` güncellenir
     - `ownerType` güncellenir (USER → ORGANIZATION veya tersi)

### Migration Stratejisi

Mevcut repository'ler için:
1. Tüm mevcut repository'lerin `githubRepoId` değerleri kontrol edilir
2. Eğer `githubRepoId` null ise, GitHub API'den fetch edilir
3. `githubRepoId` unique constraint'i zaten mevcut, migration gerekmez

### Database Schema

```java
@Column(name = "github_repo_id", nullable = false, unique = true)
private Long githubRepoId;
```

Zaten unique constraint mevcut, ek bir değişiklik gerekmez.

## Sonuç

**Karar: `githubRepoId` kullanılacak**

Bu karar:
- ✅ Repository uniqueness için canonical key olarak `githubRepoId` kullanılır
- ✅ Deduplication logic'i `githubRepoId` bazlı çalışır
- ✅ Rename/transfer senaryolarında repository aynı kalır, metadata güncellenir
- ✅ Performans ve veri tutarlılığı açısından optimal çözümdür

## Referanslar

- [GitHub API - Repository Object](https://docs.github.com/en/rest/repos/repos#get-a-repository)
- Repository Entity: `src/main/java/com/optimetric/github/infrastructure/persistence/entity/RepositoryEntity.java`
- RepositoryPersistenceAdapter: `src/main/java/com/optimetric/github/infrastructure/persistence/adapter/RepositoryPersistenceAdapter.java`

## Notlar

- Bu karar ISSUE 7 kapsamında dokümante edilmiştir
- Uygulama için ayrı bir task/PR oluşturulacaktır
- Migration planı ayrıca dokümante edilecektir
