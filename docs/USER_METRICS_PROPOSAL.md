# Kullanıcı Metrikleri Önerisi

Mevcut entity'lere dayanarak oluşturulabilecek basit ve faydalı kullanıcı metrikleri.

## 📊 Mevcut Veri Kaynakları

### Entity'ler:
1. **RepositoryEntity** - Repo bilgileri, star/fork sayıları, tarihler
2. **PullRequestEntity** - PR'lar, state, change stats, tarihler
3. **PullRequestReviewEntity** - Review'lar, state, reviewer bilgileri
4. **PullRequestCommitEntity** - PR commit'leri
5. **CommitEntity** - Commit'ler, author/committer bilgileri
6. **LanguageEntity** - Repo dilleri ve yüzdeleri
7. **BranchEntity** - Branch'ler
8. **User** - Kullanıcı bilgileri

---

## 🎯 Önerilen Kullanıcı Metrikleri

### 1. **Repository Metrikleri** (RepositoryEntity)

#### 1.1 Toplam Repository Sayısı
```java
- Toplam repo sayısı (user'a ait)
- Public/Private repo dağılımı
- Fork/Original repo dağılımı
```

#### 1.2 Repository Popülerlik Metrikleri
```java
- Toplam star sayısı (tüm repo'lar)
- Toplam fork sayısı
- Ortalama star/repo
- En çok star alan repo'lar (top 5)
```

#### 1.3 Repository Aktivite Metrikleri
```java
- Son 30 günde güncellenen repo sayısı
- En aktif repo (son pushed_at'e göre)
- Repo yaş ortalaması (created_at'e göre)
```

#### 1.4 Repository Boyut Metrikleri
```java
- Toplam repo boyutu (KB)
- Ortalama repo boyutu
- En büyük repo'lar (top 5)
```

---

### 2. **Pull Request Metrikleri** (PullRequestEntity)

#### 2.1 PR İstatistikleri
```java
- Toplam PR sayısı (user'ın repo'larında)
- Açık/Kapalı PR dağılımı
- Merge edilen PR sayısı
- Draft PR sayısı
```

#### 2.2 PR Aktivite Metrikleri
```java
- Son 30 günde açılan PR sayısı
- Son 30 günde merge edilen PR sayısı
- Ortalama PR yaşam süresi (created_at -> merged_at)
- En hızlı merge edilen PR'lar
```

#### 2.3 PR Değişiklik Metrikleri
```java
- Toplam eklenen satır sayısı (additions)
- Toplam silinen satır sayısı (deletions)
- Toplam değiştirilen dosya sayısı
- Ortalama PR boyutu (additions + deletions)
- En büyük PR'lar (top 5)
```

#### 2.4 PR Yazar Metrikleri
```java
- User'ın yazdığı PR sayısı (author_login'e göre)
- User'ın merge ettiği PR sayısı
```

---

### 3. **Review Metrikleri** (PullRequestReviewEntity)

#### 3.1 Review İstatistikleri
```java
- Toplam review sayısı (user'ın repo'larında)
- Approved/Changes Requested/Comment dağılımı
- User'ın yaptığı review sayısı (reviewer_login'e göre)
```

#### 3.2 Review Aktivite Metrikleri
```java
- Son 30 günde yapılan review sayısı
- Ortalama review süresi (PR created_at -> review submitted_at)
- En aktif reviewer'lar (top 5)
```

---

### 4. **Commit Metrikleri** (CommitEntity)

#### 4.1 Commit İstatistikleri
```java
- Toplam commit sayısı (user'ın repo'larında)
- User'ın yazdığı commit sayısı (author_login'e göre)
- Merge commit sayısı
```

#### 4.2 Commit Aktivite Metrikleri
```java
- Son 30 günde yapılan commit sayısı
- Günlük commit dağılımı (son 30 gün)
- En aktif günler
- Ortalama commit mesaj uzunluğu
```

#### 4.3 Commit Zaman Analizi
```java
- Commit saatleri dağılımı (0-23)
- Hafta içi/hafta sonu commit oranı
- En aktif saatler
```

---

### 5. **Language Metrikleri** (LanguageEntity)

#### 5.1 Dil Dağılımı
```java
- Kullanılan diller (tüm repo'lar)
- Her dilin toplam byte miktarı
- Her dilin yüzdesi (tüm repo'lar genelinde)
- En çok kullanılan diller (top 5)
```

#### 5.2 Dil Çeşitliliği
```java
- Kullanılan farklı dil sayısı
- Tek dil kullanan repo sayısı
- Çok dilli repo sayısı (3+ dil)
```

---

### 6. **Branch Metrikleri** (BranchEntity)

#### 6.1 Branch İstatistikleri
```java
- Toplam branch sayısı (user'ın repo'larında)
- Repo başına ortalama branch sayısı
- En çok branch'e sahip repo'lar
```

---

### 7. **Kombine Metrikler** (Çoklu Entity)

#### 7.1 Genel Aktivite Skoru
```java
- Son 30 günde: commit + PR + review sayısı
- Aktivite trendi (haftalık)
```

#### 7.2 Kod Kalitesi Göstergeleri
```java
- PR merge oranı (merged / total)
- Ortalama review sayısı per PR
- Ortalama PR boyutu (küçük PR'lar daha iyi)
```

#### 7.3 Topluluk Etkileşimi
```java
- Toplam star sayısı
- Toplam fork sayısı
- PR review sayısı
- Topluluk skoru (star + fork + review)
```

#### 7.4 Zaman Bazlı Trendler
```java
- Aylık commit trendi (son 6 ay)
- Aylık PR trendi
- Aylık review trendi
- Aktivite büyüme oranı
```

---

## 🏗️ Önerilen Implementasyon Yapısı

### 1. MetricServicePort (Interface)
```java
public interface UserMetricsServicePort {
    UserMetricsDto getUserMetrics(String login);
    RepositoryMetricsDto getRepositoryMetrics(String login);
    PullRequestMetricsDto getPullRequestMetrics(String login);
    CommitMetricsDto getCommitMetrics(String login);
    LanguageMetricsDto getLanguageMetrics(String login);
    ActivityMetricsDto getActivityMetrics(String login, int days);
}
```

### 2. Metric DTO'ları
```java
- UserMetricsDto (genel özet)
- RepositoryMetricsDto
- PullRequestMetricsDto
- CommitMetricsDto
- LanguageMetricsDto
- ActivityMetricsDto
- TrendMetricsDto
```

### 3. MetricController
```java
@PostMapping("/metrics/user")
@PostMapping("/metrics/repositories")
@PostMapping("/metrics/pull-requests")
@PostMapping("/metrics/commits")
@PostMapping("/metrics/languages")
@PostMapping("/metrics/activity")
```

---

## 📈 Öncelikli Metrikler (MVP)

### Yüksek Öncelik:
1. ✅ Toplam repo sayısı
2. ✅ Toplam PR sayısı (açık/kapalı/merged)
3. ✅ Toplam commit sayısı
4. ✅ Toplam star/fork sayısı
5. ✅ En çok kullanılan diller (top 5)
6. ✅ Son 30 gün aktivite (commit + PR)

### Orta Öncelik:
7. PR merge oranı
8. Ortalama PR boyutu
9. Review istatistikleri
10. Commit zaman analizi

### Düşük Öncelik:
11. Trend analizleri
12. Detaylı dil dağılımı
13. Branch metrikleri

---

## 💡 Örnek Response Yapısı

```json
{
  "userMetrics": {
    "totalRepositories": 15,
    "totalStars": 1250,
    "totalForks": 89,
    "totalPullRequests": 234,
    "openPullRequests": 12,
    "mergedPullRequests": 198,
    "totalCommits": 1523,
    "totalReviews": 456,
    "topLanguages": [
      {"name": "Java", "percentage": 45.2, "bytes": 1250000},
      {"name": "TypeScript", "percentage": 30.5, "bytes": 845000}
    ],
    "last30DaysActivity": {
      "commits": 45,
      "pullRequests": 12,
      "reviews": 23
    }
  }
}
```

---

## 🔧 Teknik Notlar

1. **Performance**: Büyük veri setleri için pagination ve caching gerekebilir
2. **Aggregation**: JPA native queries veya database view'ları kullanılabilir
3. **Caching**: Metrikler sık değişmediği için Redis cache önerilir
4. **Background Jobs**: Ağır hesaplamalar için async job'lar kullanılabilir
5. **Incremental Updates**: Metrikleri real-time değil, periyodik güncelleme yapılabilir

---

## 📝 Sonuç

Mevcut entity'ler ile **15+ farklı metrik** oluşturulabilir. MVP için **6 temel metrik** ile başlanabilir ve zamanla genişletilebilir.

