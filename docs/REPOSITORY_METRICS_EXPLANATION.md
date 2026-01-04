# Repository Metrikleri Açıklaması

## 📊 Metrik Kategorileri ve Açıklamaları

### 1. Repository Overview (Genel Bakış)

Repository'nin temel bilgileri, popülerlik metrikleri ve özelliklerini içerir.

#### Temel Bilgiler
- **fullName**: Repository'nin tam adı (owner/repo formatında)
- **name**: Repository adı
- **description**: Repository açıklaması
- **htmlUrl**: GitHub URL'i
- **ownerLogin**: Repository sahibinin login'i
- **ownerType**: Sahip tipi (USER veya ORGANIZATION)
- **defaultBranch**: Varsayılan branch adı
- **primaryLanguage**: Ana programlama dili

#### Popülerlik Metrikleri
- **stars**: Yıldız sayısı (topluluk ilgisi göstergesi)
- **forks**: Fork sayısı (repo'nun ne kadar kopyalandığı)
- **watchers**: İzleyici sayısı
- **openIssues**: Açık issue sayısı

#### Repository Özellikleri
- **isPrivate**: Private repo mu? (true/false)
- **isFork**: Fork edilmiş repo mu? (true/false)
- **size**: Repository boyutu (KB cinsinden)

#### Tarih Bilgileri
- **createdAt**: Repository oluşturulma tarihi
- **updatedAt**: Son güncelleme tarihi
- **pushedAt**: Son push tarihi
- **lastSyncedAt**: Sistemde son senkronizasyon tarihi

#### Hesaplanan Metrikler
- **repositoryAgeDays**: Repository yaşı (gün cinsinden)
- **isActive**: Son 30 günde aktivite var mı? (true/false)
- **daysSinceLastPush**: Son push'tan bu yana geçen gün sayısı

---

### 2. Pull Request Metrics (PR Metrikleri)

Repository'deki Pull Request'lerin detaylı analizini içerir.

#### PR İstatistikleri
- **totalPullRequests**: Toplam PR sayısı
- **openPullRequests**: Açık PR sayısı
- **closedPullRequests**: Kapatılmış PR sayısı (merge edilmemiş)
- **mergedPullRequests**: Merge edilmiş PR sayısı
- **draftPullRequests**: Draft (taslak) PR sayısı
- **mergeRate**: Merge oranı yüzdesi (0-100). `mergedPullRequests / totalPullRequests * 100`

#### PR Boyut Metrikleri
- **averageAdditions**: Ortalama eklenen satır sayısı
- **averageDeletions**: Ortalama silinen satır sayısı
- **averageChangedFiles**: Ortalama değiştirilen dosya sayısı
- **averageTotalChanges**: Ortalama toplam değişiklik (additions + deletions)
- **largestPrSize**: En büyük PR boyutu (satır sayısı)
- **smallestPrSize**: En küçük PR boyutu (satır sayısı)

#### PR Boyut Dağılımı
- **smallPrCount**: Küçük PR sayısı (< 100 satır)
- **mediumPrCount**: Orta PR sayısı (100-500 satır arası)
- **largePrCount**: Büyük PR sayısı (> 500 satır)
- **smallPrPercentage**: Küçük PR'ların yüzdesi
- **mediumPrPercentage**: Orta PR'ların yüzdesi
- **largePrPercentage**: Büyük PR'ların yüzdesi

**Not:** PR boyutu = additions + deletions

#### PR Aktivite Metrikleri
- **prsLast30Days**: Son 30 günde açılan PR sayısı
- **prsLast7Days**: Son 7 günde açılan PR sayısı
- **mergedLast30Days**: Son 30 günde merge edilen PR sayısı
- **averageMergeTimeHours**: PR'ın oluşturulmasından merge edilmesine kadar geçen ortalama süre (saat)

#### PR Yazar Metrikleri
- **uniqueAuthors**: Farklı PR yazarı sayısı
- **topAuthors**: En aktif PR yazarları listesi (top 10)
  - `authorLogin`: Yazar login'i
  - `prCount`: Yazarın toplam PR sayısı
  - `mergedPrCount`: Yazarın merge edilen PR sayısı
  - `mergeRate`: Yazarın merge oranı
- **prsByAuthor**: Yazara göre PR dağılımı (Map: author -> count)

---

### 3. Commit Metrics (Commit Metrikleri)

Repository'deki commit'lerin detaylı analizini içerir.

#### Commit İstatistikleri
- **totalCommits**: Toplam commit sayısı
- **mergeCommits**: Merge commit sayısı
- **regularCommits**: Normal commit sayısı
- **mergeCommitPercentage**: Merge commit yüzdesi (0-100)

#### Commit Aktivite Metrikleri
- **commitsLast30Days**: Son 30 günde yapılan commit sayısı
- **commitsLast7Days**: Son 7 günde yapılan commit sayısı
- **lastCommitDate**: Son commit tarihi
- **mostActiveDay**: En aktif gün (MONDAY, TUESDAY, vb.)
- **mostActiveHour**: En aktif saat (0-23 arası)

#### Commit Zaman Analizi
- **commitsByHour**: Saate göre commit dağılımı (Map: hour 0-23 -> count)
- **commitsByDayOfWeek**: Haftanın gününe göre commit dağılımı (Map: day -> count)
- **weekdayCommits**: Hafta içi commit sayısı
- **weekendCommits**: Hafta sonu commit sayısı
- **weekdayPercentage**: Hafta içi commit yüzdesi
- **weekendPercentage**: Hafta sonu commit yüzdesi

#### Commit Yazar Metrikleri
- **uniqueAuthors**: Farklı commit yazarı sayısı
- **topAuthors**: En aktif commit yazarları listesi (top 10)
  - `authorLogin`: Yazar login'i
  - `commitCount`: Yazarın toplam commit sayısı
  - `mergeCommitCount`: Yazarın merge commit sayısı
- **commitsByAuthor**: Yazara göre commit dağılımı (Map: author -> count)

#### Commit Mesaj Analizi
- **averageMessageLength**: Ortalama commit mesaj uzunluğu (karakter sayısı)
- **longestMessageLength**: En uzun commit mesaj uzunluğu
- **shortestMessageLength**: En kısa commit mesaj uzunluğu

---

### 4. Review Metrics (Review Metrikleri)

Repository'deki PR review'larının detaylı analizini içerir.

#### Review İstatistikleri
- **totalReviews**: Toplam review sayısı
- **approvedReviews**: Onaylanmış (APPROVED) review sayısı
- **changesRequestedReviews**: Değişiklik istenen (CHANGES_REQUESTED) review sayısı
- **commentedReviews**: Sadece yorum yapılan (COMMENTED) review sayısı
- **reviewsByState**: State'e göre review dağılımı (Map: state -> count)

#### Review Aktivite Metrikleri
- **reviewsLast30Days**: Son 30 günde yapılan review sayısı
- **reviewsLast7Days**: Son 7 günde yapılan review sayısı
- **averageReviewsPerPr**: PR başına ortalama review sayısı
- **prsWithoutReviews**: Review almayan PR sayısı
- **prsWithReviewsPercentage**: Review alan PR'ların yüzdesi
- **approvalRate**: Onay oranı yüzdesi (0-100). `approvedReviews / totalReviews * 100`

#### Reviewer Metrikleri
- **uniqueReviewers**: Farklı reviewer sayısı
- **topReviewers**: En aktif reviewer'lar listesi (top 10)
  - `reviewerLogin`: Reviewer login'i
  - `reviewCount`: Reviewer'ın toplam review sayısı
  - `approvalCount`: Reviewer'ın onay sayısı
  - `approvalRate`: Reviewer'ın onay oranı
- **reviewsByReviewer**: Reviewer'a göre review dağılımı (Map: reviewer -> count)
- **approvalsByReviewer**: Reviewer'a göre onay dağılımı (Map: reviewer -> approval count)

---

### 5. Branch Metrics (Branch Metrikleri)

Repository'deki branch'lerin analizini içerir.

#### Branch İstatistikleri
- **totalBranches**: Toplam branch sayısı
- **defaultBranch**: Varsayılan branch adı

#### Branch Aktivite Metrikleri
- **topBranchesByCommits**: Commit sayısına göre en aktif branch'ler (top 10)
  - `branchName`: Branch adı
  - `commitCount`: Branch'teki commit sayısı
  - `lastCommitDate`: Branch'teki son commit tarihi
  - `isDefault`: Varsayılan branch mi? (true/false)
- **commitsByBranch**: Branch'e göre commit dağılımı (Map: branch -> commit count)
- **branchesWithNoCommits**: Commit'i olmayan branch sayısı
- **averageCommitsPerBranch**: Branch başına ortalama commit sayısı

---

### 6. Language Metrics (Dil Metrikleri)

Repository'deki programlama dillerinin analizini içerir.

#### Dil Dağılımı
- **totalLanguages**: Toplam farklı dil sayısı
- **primaryLanguage**: Ana dil (en çok kullanılan)
- **primaryLanguagePercentage**: Ana dilin yüzdesi (0-100)
- **languageDistribution**: Dil yüzde dağılımı (Map: language -> percentage)
- **languageBytes**: Dil byte dağılımı (Map: language -> bytes)

#### Dil Detayları
- **topLanguages**: En çok kullanılan diller listesi (top 10)
  - `language`: Dil adı
  - `bytes`: Dil byte miktarı
  - `percentage`: Dil yüzdesi
- **isMultiLanguage**: Çok dilli mi? (2+ dil varsa true)
- **languageDiversityScore**: Dil çeşitliliği skoru (0-100). Daha fazla dil = daha yüksek skor

---

### 7. Activity Trend (Aktivite Trendleri)

Repository'nin zaman bazlı aktivite trendlerini içerir.

#### Haftalık Trend
- **weeklyTrend**: Son 12 haftalık haftalık aktivite trendi
  - `week`: Hafta formatı (YYYY-WW, örn: "2024-W01")
  - `commits`: Bu haftadaki commit sayısı
  - `pullRequests`: Bu haftadaki PR sayısı
  - `reviews`: Bu haftadaki review sayısı
  - `totalActivity`: Bu haftadaki toplam aktivite

#### Aylık Trend
- **monthlyTrend**: Son 6 aylık aylık aktivite trendi
  - `month`: Ay formatı (YYYY-MM, örn: "2024-01")
  - `commits`: Bu aydaki commit sayısı
  - `pullRequests`: Bu aydaki PR sayısı
  - `reviews`: Bu aydaki review sayısı
  - `totalActivity`: Bu aydaki toplam aktivite

#### Son Aktivite
- **totalActivityLast30Days**: Son 30 gündeki toplam aktivite (commit + PR + review)
- **totalActivityLast7Days**: Son 7 gündeki toplam aktivite
- **last30DaysBreakdown**: Son 30 günün detaylı aktivite dağılımı
  - `commits`: Commit sayısı
  - `pullRequests`: PR sayısı
  - `reviews`: Review sayısı

#### Büyüme Metrikleri
- **activityGrowthRate**: Aktivite büyüme oranı yüzdesi (son 30 gün vs önceki 30 gün)
- **mostActiveWeek**: En aktif hafta (YYYY-WW formatında)
- **mostActiveMonth**: En aktif ay (YYYY-MM formatında)

#### Günlük Dağılım
- **commitsByDay**: Günlere göre commit dağılımı (Map: YYYY-MM-DD -> count)
- **prsByDay**: Günlere göre PR dağılımı (Map: YYYY-MM-DD -> count)
- **reviewsByDay**: Günlere göre review dağılımı (Map: YYYY-MM-DD -> count)

---

### 8. Code Quality Metrics (Kod Kalitesi Metrikleri)

Repository'nin kod kalitesi göstergelerini içerir.

#### PR Kalite Göstergeleri
- **averagePrSize**: Ortalama PR boyutu (satır sayısı). Küçük PR'lar daha iyi
- **prMergeRate**: PR merge oranı (0-100). Yüksek oran iyi
- **averageReviewsPerPr**: PR başına ortalama review sayısı. Yeterli review önemli
- **prsWithoutReviewsPercentage**: Review almayan PR yüzdesi. Düşük olmalı
- **smallPrPercentage**: Küçük PR yüzdesi (< 100 satır). Yüksek olmalı

#### Commit Kalite Göstergeleri
- **mergeCommitPercentage**: Merge commit yüzdesi. Düşük olmalı
- **averageCommitMessageLength**: Ortalama commit mesaj uzunluğu. Makul uzunlukta olmalı
- **regularCommitPercentage**: Normal commit yüzdesi. Yüksek olmalı

#### Kod Değişiklik Metrikleri
- **netCodeChange**: Net kod değişikliği (additions - deletions). Pozitif = büyüme, negatif = küçülme
- **codeGrowthRate**: Kod büyüme oranı yüzdesi
- **averageFilesChangedPerPr**: PR başına ortalama değiştirilen dosya sayısı

#### Genel Kalite Skoru
- **overallQualityScore**: Genel kod kalitesi skoru (0-100)
  - PR kalitesi: %40 ağırlık
  - Commit kalitesi: %30 ağırlık
  - Kod değişiklik kalitesi: %30 ağırlık

---

### 9. Contributor Metrics (Katkıda Bulunanlar Metrikleri)

Repository'ye katkıda bulunanların analizini içerir.

#### Contributor İstatistikleri
- **totalContributors**: Toplam farklı contributor sayısı
- **activeContributorsLast30Days**: Son 30 günde aktif olan contributor sayısı
- **activeContributorsLast7Days**: Son 7 günde aktif olan contributor sayısı

#### Top Contributor'lar
- **topContributorsByCommits**: Commit sayısına göre en aktif contributor'lar (top 10)
- **topContributorsByPrs**: PR sayısına göre en aktif contributor'lar (top 10)
- **topContributorsByReviews**: Review sayısına göre en aktif contributor'lar (top 10)

Her contributor için:
  - `contributorLogin`: Contributor login'i
  - `commitCount`: Commit sayısı
  - `prCount`: PR sayısı
  - `reviewCount`: Review sayısı
  - `totalContributions`: Toplam katkı (commit + PR + review)

#### Contributor Dağılımı
- **commitsByContributor**: Contributor'a göre commit dağılımı (Map: contributor -> count)
- **prsByContributor**: Contributor'a göre PR dağılımı (Map: contributor -> count)
- **reviewsByContributor**: Contributor'a göre review dağılımı (Map: contributor -> count)

#### Aktivite Dağılımı (Pareto Analizi)
- **top10ContributorsPercentage**: En aktif 10 contributor'ın toplam aktivite yüzdesi
- **singleCommitContributors**: Sadece 1 commit yapan contributor sayısı
- **regularContributors**: 5+ commit yapan düzenli contributor sayısı

---

### 10. Repository Health Score (Sağlık Skoru)

Repository'nin genel sağlık durumunu gösteren skor.

#### Bireysel Skorlar (0-100)
- **activityScore**: Aktivite skoru (son 30 gün aktiviteye göre)
  - 100+ aktivite: 100
  - 50-99 aktivite: 80
  - 20-49 aktivite: 60
  - 10-19 aktivite: 40
  - < 10 aktivite: 20

- **popularityScore**: Popülerlik skoru (star + fork + watcher sayısına göre)
  - 1000+ toplam: 100
  - 500-999: 80
  - 100-499: 60
  - 50-99: 40
  - 10-49: 20
  - < 10: 10

- **codeQualityScore**: Kod kalitesi skoru (Code Quality Metrics'ten alınır)

- **communityScore**: Topluluk skoru (contributor sayısına göre)
  - 10+ contributor, 5+ aktif: 100
  - 5-9 contributor, 3+ aktif: 80
  - 3-4 contributor, 2+ aktif: 60
  - 2 contributor: 40
  - 1 contributor: 20
  - 0 contributor: 0

#### Genel Sağlık Skoru
- **overallHealthScore**: Genel sağlık skoru (0-100)
  - Aktivite: %30 ağırlık
  - Popülerlik: %20 ağırlık
  - Kod Kalitesi: %30 ağırlık
  - Topluluk: %20 ağırlık

#### Sağlık Durumu
- **healthStatus**: Sağlık durumu kategorisi
  - `EXCELLENT`: 80-100
  - `GOOD`: 60-79
  - `FAIR`: 40-59
  - `POOR`: 20-39
  - `CRITICAL`: 0-19

#### Ağırlık Dağılımı
- **breakdown**: Skorların ağırlık dağılımı
  - `activityWeight`: 0.3
  - `popularityWeight`: 0.2
  - `qualityWeight`: 0.3
  - `communityWeight`: 0.2

---

## 📝 Genel Notlar

1. **Yüzde Değerleri:** Tüm yüzde alanları 0-100 arası değerler içerir (örn: 84.62 = %84.62)

2. **Tarih Formatları:**
   - Hafta: `YYYY-WW` (örn: "2024-W01")
   - Ay: `YYYY-MM` (örn: "2024-01")
   - Gün: `YYYY-MM-DD` (örn: "2024-01-15")
   - ISO Format: `YYYY-MM-DDTHH:mm:ssZ` (örn: "2024-02-15T14:30:00Z")

3. **Saat Formatı:** `commitsByHour` içinde saat 0-23 arası değerler (0 = gece yarısı, 23 = 23:00)

4. **Haftanın Günleri:** `commitsByDayOfWeek` içinde İngilizce gün isimleri (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)

5. **Null Değerler:** Eğer veri yoksa, sayısal alanlar 0, yüzde alanları 0.00, listeler boş array, map'ler boş map döner.

6. **Hesaplama Mantığı:**
   - Tüm metrikler tek bir repository üzerinden hesaplanır
   - Sadece mevcut veriler kullanılır (null olanlar hariç tutulur)
   - Ortalamalar BigDecimal ile hesaplanır (2-4 ondalık basamak)
   - Yüzdeler 4 ondalık basamakla hesaplanır

7. **Performans:**
   - Tek repo için hesaplamalar user metriklerinden daha hızlıdır
   - `@Transactional(readOnly = true)` ile optimize edilmiştir
   - Büyük repo'lar için response süresi artabilir

