# Repository Bazlı Metrikler Analizi

Tek bir repository için (repoFullName parametresi ile) oluşturulabilecek faydalı metrikler analizi.

## 📊 Mevcut Entity Verileri

### RepositoryEntity (Tek Repo)
- `fullName`, `name`, `description`
- `stargazersCount`, `forksCount`, `watchersCount`
- `openIssuesCount`, `size` (KB)
- `language` (ana dil), `defaultBranch`
- `isPrivate`, `isFork`
- `createdAt`, `updatedAt`, `pushedAt`
- `ownerLogin`, `ownerType`

### PullRequestEntity (Bu Repo'ya Ait)
- `prNumber`, `state`, `draft`
- `additions`, `deletions`, `changedFiles`
- `authorLogin`, `authorGithubId`
- `createdAt`, `updatedAt`, `closedAt`, `mergedAt`
- `title`, `body`

### CommitEntity (Bu Repo'ya Ait)
- `sha`, `message`
- `authorLogin`, `committerLogin`
- `authorName`, `committerName`
- `authoredAt`, `committedAt`
- `isMergeCommit`
- `branch` (hangi branch'te)

### PullRequestReviewEntity (Bu Repo'nun PR'larına Ait)
- `reviewerLogin`, `reviewerGithubId`
- `state` (APPROVED, CHANGES_REQUESTED, COMMENTED)
- `submittedAt`, `body`

### PullRequestCommitEntity (Bu Repo'nun PR'larına Ait)
- `sha`, `message`
- `authorLogin`, `committedAt`

### LanguageEntity (Bu Repo'ya Ait)
- `languageName`, `bytes`, `percentage`

### BranchEntity (Bu Repo'ya Ait)
- `name`

---

## 🎯 Önerilen Repository Metrikleri

### 1. **Repository Overview Metrics** (Genel Bakış)

#### 1.1 Temel Bilgiler
```java
- Repo adı, fullName, description
- Oluşturulma tarihi, yaşı (gün cinsinden)
- Son güncelleme tarihi
- Son push tarihi
- Repo durumu (aktif/pasif)
```

#### 1.2 Popülerlik Metrikleri
```java
- Star sayısı
- Fork sayısı
- Watcher sayısı
- Open issues sayısı
- Star/Fork oranı
- Popülerlik skoru (star + fork + watcher)
```

#### 1.3 Repo Özellikleri
```java
- Private/Public durumu
- Fork mu? Original mu?
- Ana dil
- Default branch
- Repo boyutu (KB/MB)
```

---

### 2. **Pull Request Metrics** (PR Metrikleri)

#### 2.1 PR İstatistikleri
```java
- Toplam PR sayısı
- Açık PR sayısı
- Kapalı PR sayısı
- Merge edilen PR sayısı
- Draft PR sayısı
- Merge oranı (%)
- Ortalama PR yaşam süresi (oluşturulma -> merge)
```

#### 2.2 PR Değişiklik Metrikleri
```java
- Toplam eklenen satır sayısı (tüm PR'lar)
- Toplam silinen satır sayısı
- Toplam değiştirilen dosya sayısı
- Ortalama PR boyutu (additions + deletions)
- En büyük PR (satır sayısı)
- En küçük PR (satır sayısı)
- PR boyut dağılımı (küçük/orta/büyük)
```

#### 2.3 PR Aktivite Metrikleri
```java
- Son 30 günde açılan PR sayısı
- Son 30 günde merge edilen PR sayısı
- Son 30 günde kapatılan PR sayısı
- En aktif ay (en çok PR açılan)
- PR açılma trendi (haftalık/aylık)
```

#### 2.4 PR Yazar Metrikleri
```java
- Toplam farklı PR yazarı sayısı
- En aktif PR yazarları (top 5)
- Yazar başına ortalama PR sayısı
- En çok merge edilen yazarlar
```

---

### 3. **Commit Metrics** (Commit Metrikleri)

#### 3.1 Commit İstatistikleri
```java
- Toplam commit sayısı
- Merge commit sayısı
- Regular commit sayısı
- Merge commit oranı (%)
```

#### 3.2 Commit Aktivite Metrikleri
```java
- Son 30 günde commit sayısı
- Son 7 günde commit sayısı
- Son commit tarihi
- En aktif gün (en çok commit yapılan)
- En aktif saat (0-23)
- Hafta içi/hafta sonu commit oranı
- Commit trendi (haftalık/aylık)
```

#### 3.3 Commit Yazar Metrikleri
```java
- Toplam farklı commit yazarı sayısı
- En aktif commit yazarları (top 5)
- Yazar başına ortalama commit sayısı
- Author vs Committer farkı (eğer varsa)
```

#### 3.4 Commit Mesaj Analizi
```java
- Ortalama commit mesaj uzunluğu
- En uzun commit mesajı
- En kısa commit mesajı
- Commit mesaj kalitesi (karakter sayısına göre)
```

---

### 4. **Review Metrics** (Review Metrikleri)

#### 4.1 Review İstatistikleri
```java
- Toplam review sayısı
- Approved review sayısı
- Changes Requested review sayısı
- Commented review sayısı
- Review dağılımı (state'e göre)
- Approval oranı (%)
```

#### 4.2 Review Aktivite Metrikleri
```java
- Son 30 günde review sayısı
- PR başına ortalama review sayısı
- Review almayan PR sayısı
- Review alan PR yüzdesi
- Ortalama review süresi (PR oluşturulma -> ilk review)
```

#### 4.3 Reviewer Metrikleri
```java
- Toplam farklı reviewer sayısı
- En aktif reviewer'lar (top 5)
- Reviewer başına ortalama review sayısı
- En çok onay veren reviewer'lar
```

---

### 5. **Branch Metrics** (Branch Metrikleri)

#### 5.1 Branch İstatistikleri
```java
- Toplam branch sayısı
- Default branch adı
- En uzun yaşayan branch (oluşturulma tarihine göre)
- En yeni branch
```

#### 5.2 Branch Aktivite Metrikleri
```java
- Branch başına ortalama commit sayısı
- En aktif branch (en çok commit olan)
- En az aktif branch
- Branch kullanım dağılımı
```

---

### 6. **Language Metrics** (Dil Metrikleri)

#### 6.1 Dil Dağılımı
```java
- Kullanılan diller listesi
- Her dilin byte miktarı
- Her dilin yüzdesi
- Ana dil (en çok kullanılan)
- Dil çeşitliliği skoru (kaç farklı dil)
```

#### 6.2 Dil Detayları
```java
- Top 5 dil (byte'a göre)
- Dil yüzde dağılımı
- Tek dilli mi? Çok dilli mi?
- En büyük dil (byte cinsinden)
```

---

### 7. **Activity Trends** (Aktivite Trendleri)

#### 7.1 Zaman Bazlı Trendler
```java
- Son 12 haftalık commit trendi
- Son 6 aylık PR trendi
- Son 6 aylık review trendi
- Aktivite büyüme oranı
- En aktif hafta/ay
```

#### 7.2 Aktivite Dağılımı
```java
- Son 30 gün aktivite özeti (commit + PR + review)
- Günlük aktivite dağılımı
- Haftalık aktivite dağılımı
- Aktivite yoğunluğu (yoğun/normal/düşük)
```

---

### 8. **Code Quality Metrics** (Kod Kalitesi Metrikleri)

#### 8.1 PR Kalite Göstergeleri
```java
- Ortalama PR boyutu (küçük PR'lar daha iyi)
- PR merge oranı (yüksek oran iyi)
- Ortalama review sayısı per PR (yeterli review)
- Review almayan PR yüzdesi (düşük olmalı)
```

#### 8.2 Commit Kalite Göstergeleri
```java
- Ortalama commit mesaj uzunluğu
- Merge commit oranı (düşük olmalı)
- Commit sıklığı (düzenli commit'ler)
```

#### 8.3 Kod Değişiklik Metrikleri
```java
- Net kod değişikliği (additions - deletions)
- Toplam kod büyümesi/azalması
- Ortalama dosya değişikliği per PR
```

---

### 9. **Contributor Metrics** (Katkıda Bulunanlar)

#### 9.1 Contributor İstatistikleri
```java
- Toplam farklı contributor sayısı
- En aktif contributor'lar (top 5)
- Contributor başına ortalama commit sayısı
- Contributor başına ortalama PR sayısı
```

#### 9.2 Contributor Dağılımı
```java
- En çok commit yapan contributor'lar
- En çok PR açan contributor'lar
- En çok review yapan contributor'lar
- Contributor aktivite dağılımı (Pareto analizi)
```

---

### 10. **Repository Health Score** (Repo Sağlık Skoru)

#### 10.1 Sağlık Göstergeleri
```java
- Aktivite skoru (son 30 gün aktivite)
- Popülerlik skoru (star + fork)
- Kod kalitesi skoru (PR merge oranı, review oranı)
- Topluluk katılım skoru (contributor sayısı, review sayısı)
- Genel sağlık skoru (tüm skorların ortalaması)
```

---

## 📋 Öncelikli Metrikler (MVP)

### Yüksek Öncelik (Temel Metrikler):
1. ✅ **Repository Overview** - Temel bilgiler, popülerlik
2. ✅ **PR Statistics** - Toplam, açık/kapalı, merge oranı
3. ✅ **Commit Statistics** - Toplam commit, son aktivite
4. ✅ **Language Distribution** - Dil dağılımı ve yüzdeleri
5. ✅ **Activity Trends** - Son 30 gün aktivite, trend analizi

### Orta Öncelik (Detaylı Metrikler):
6. **PR Size Metrics** - PR boyut dağılımı, ortalama
7. **Review Statistics** - Review sayıları, approval oranı
8. **Commit Time Analysis** - Saat/gün dağılımı
9. **Contributor Metrics** - En aktif contributor'lar
10. **Branch Metrics** - Branch sayısı ve aktivitesi

### Düşük Öncelik (Gelişmiş Metrikler):
11. **Code Quality Score** - Kod kalitesi göstergeleri
12. **Repository Health Score** - Genel sağlık skoru
13. **Detailed Trends** - Haftalık/aylık detaylı trendler
14. **Commit Message Analysis** - Mesaj kalitesi analizi

---

## 🏗️ Önerilen API Yapısı

### Endpoint: `POST /api/metrics/repository`

**Request:**
```json
{
  "repoFullName": "owner/repo-name"
}
```

**Response:** `RepositoryMetricsDto` (tüm metrikleri içerir)

### Alt Endpoint'ler:
- `POST /api/metrics/repository/overview` - Genel bakış
- `POST /api/metrics/repository/pull-requests` - PR metrikleri
- `POST /api/metrics/repository/commits` - Commit metrikleri
- `POST /api/metrics/repository/reviews` - Review metrikleri
- `POST /api/metrics/repository/languages` - Dil metrikleri
- `POST /api/metrics/repository/activity` - Aktivite trendleri
- `POST /api/metrics/repository/contributors` - Contributor metrikleri
- `POST /api/metrics/repository/health` - Sağlık skoru

---

## 💡 Örnek Response Yapısı

```json
{
  "overview": {
    "fullName": "owner/repo",
    "stars": 1250,
    "forks": 89,
    "openIssues": 12,
    "size": 1024,
    "language": "Java",
    "createdAt": "2020-01-15T10:00:00Z",
    "lastPushedAt": "2024-02-15T14:30:00Z",
    "isActive": true
  },
  "pullRequests": {
    "total": 234,
    "open": 12,
    "merged": 198,
    "closed": 24,
    "mergeRate": 84.62,
    "averageSize": 214.5
  },
  "commits": {
    "total": 1523,
    "last30Days": 45,
    "last7Days": 12,
    "mostActiveHour": 15,
    "mostActiveDay": "THURSDAY"
  },
  "languages": {
    "total": 3,
    "distribution": {
      "Java": 75.2,
      "TypeScript": 20.5,
      "JavaScript": 4.3
    }
  },
  "activity": {
    "last30Days": 456,
    "trend": "increasing",
    "growthRate": 12.5
  }
}
```

---

## 🔧 Teknik Notlar

1. **Performance:** Tek repo için hesaplamalar daha hızlı olacak (kullanıcı bazlından daha az veri)

2. **Caching:** Repo metrikleri daha az sık değişir, cache'lenebilir

3. **Real-time Updates:** Repo metrikleri periyodik güncellenebilir (her saat/gün)

4. **Validation:** `repoFullName` formatı validate edilmeli (owner/repo formatında)

5. **Authorization:** Kullanıcının bu repo'ya erişim yetkisi kontrol edilmeli

---

## 📝 Sonuç

Tek bir repo için **15+ farklı metrik kategorisi** ve **50+ spesifik metrik** oluşturulabilir. MVP için **5 temel kategori** ile başlanabilir ve zamanla genişletilebilir.

