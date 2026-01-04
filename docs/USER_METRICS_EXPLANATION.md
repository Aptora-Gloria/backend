# User Metrikleri Açıklaması

## 📊 Metrik Kategorileri ve Açıklamaları

### 1. PR Merge Rate (PR Merge Oranı)

Kullanıcının tüm repo'larındaki Pull Request merge istatistiklerini içerir.

#### PR İstatistikleri
- **totalPullRequests**: Kullanıcının tüm repo'larındaki toplam PR sayısı
- **mergedPullRequests**: Merge edilmiş PR sayısı
- **openPullRequests**: Açık (open) durumundaki PR sayısı
- **closedPullRequests**: Kapatılmış ama merge edilmemiş PR sayısı

#### Merge Metrikleri
- **mergeRate**: Merge oranı yüzdesi (0-100). `mergedPullRequests / totalPullRequests * 100`
- **averageMergeTimeHours**: PR'ın oluşturulmasından merge edilmesine kadar geçen ortalama süre (saat cinsinden)

**Kullanım:** Kullanıcının PR'larının ne kadarının merge edildiğini ve merge sürelerini gösterir.

---

### 2. PR Size (PR Boyutu)

Kullanıcının PR'larının boyut analizini içerir.

#### Ortalama Boyutlar
- **averageAdditions**: Ortalama eklenen satır sayısı (tüm PR'lar için)
- **averageDeletions**: Ortalama silinen satır sayısı
- **averageChangedFiles**: Ortalama değiştirilen dosya sayısı
- **averageTotalChanges**: Ortalama toplam değişiklik (additions + deletions)

#### Boyut Dağılımı
- **smallPrCount**: Küçük PR sayısı (< 100 satır)
- **mediumPrCount**: Orta PR sayısı (100-500 satır arası)
- **largePrCount**: Büyük PR sayısı (> 500 satır)
- **smallPrPercentage**: Küçük PR'ların yüzdesi
- **mediumPrPercentage**: Orta PR'ların yüzdesi
- **largePrPercentage**: Büyük PR'ların yüzdesi

**Not:** PR boyutu = additions + deletions

**Kullanım:** Kullanıcının PR'larının genellikle ne kadar büyük olduğunu gösterir. Küçük PR'lar genellikle daha iyi kabul edilir.

---

### 3. Review Stats (Review İstatistikleri)

Kullanıcının repo'larındaki PR review istatistiklerini içerir.

#### Review İstatistikleri
- **totalReviews**: Toplam review sayısı (tüm PR'lar için)
- **approvedReviews**: Onaylanmış (APPROVED) review sayısı
- **changesRequestedReviews**: Değişiklik istenen (CHANGES_REQUESTED) review sayısı
- **commentedReviews**: Sadece yorum yapılan (COMMENTED) review sayısı

#### Review Metrikleri
- **averageReviewsPerPr**: PR başına ortalama review sayısı
- **approvalRate**: Onay oranı yüzdesi (0-100). `approvedReviews / totalReviews * 100`
- **reviewsByState**: State'e göre review dağılımı (Map: state -> count)
- **prsWithoutReviews**: Review almayan PR sayısı
- **prsWithReviewsPercentage**: Review alan PR'ların yüzdesi

**Kullanım:** Kullanıcının PR'larının ne kadar review aldığını ve review kalitesini gösterir.

---

### 4. Commit Time Analysis (Commit Zaman Analizi)

Kullanıcının commit'lerinin zaman bazlı analizini içerir.

#### Zaman Dağılımı
- **commitsByHour**: Saate göre commit dağılımı (Map: hour 0-23 -> count)
- **commitsByDayOfWeek**: Haftanın gününe göre commit dağılımı (MONDAY, TUESDAY, vb.)
- **weekdayCommits**: Hafta içi (Pazartesi-Cuma) commit sayısı
- **weekendCommits**: Hafta sonu (Cumartesi-Pazar) commit sayısı
- **weekdayPercentage**: Hafta içi commit yüzdesi
- **weekendPercentage**: Hafta sonu commit yüzdesi

#### Aktivite Metrikleri
- **mostActiveHour**: En aktif saat (0-23 arası)
- **mostActiveDay**: En aktif gün (MONDAY, TUESDAY, vb.)
- **last30DaysCommits**: Son 30 gündeki commit sayısı
- **last7DaysCommits**: Son 7 gündeki commit sayısı

**Kullanım:** Kullanıcının commit yapma alışkanlıklarını ve aktivite pattern'lerini gösterir.

---

### 5. Language Diversity (Dil Çeşitliliği)

Kullanıcının repo'larında kullanılan programlama dillerinin analizini içerir.

#### Dil İstatistikleri
- **totalLanguages**: Toplam farklı dil sayısı
- **uniqueLanguagesAcrossRepos**: Tüm repo'lar genelinde kullanılan benzersiz dil sayısı
- **averageLanguagesPerRepo**: Repo başına ortalama dil sayısı

#### Dil Dağılımı
- **languageUsageCount**: Her dilin kaç repo'da kullanıldığı (Map: language -> repo count)
- **languagePercentageDistribution**: Her dilin toplam kod içindeki yüzdesi (Map: language -> percentage)
- **topLanguages**: En çok kullanılan diller listesi (LanguageUsageDto array)
  - `language`: Dil adı
  - `repoCount`: Bu dili kullanan repo sayısı
  - `totalPercentage`: Bu dilin toplam kod içindeki yüzdesi
  - `totalBytes`: Bu dilin toplam byte miktarı

#### Çeşitlilik Metrikleri
- **singleLanguageRepos**: Tek dil kullanan repo sayısı
- **multiLanguageRepos**: Çok dilli (2+ dil) repo sayısı
- **multiLanguageReposPercentage**: Çok dilli repo'ların yüzdesi

**Kullanım:** Kullanıcının teknoloji stack'ini ve dil çeşitliliğini gösterir.

---

### 6. Activity Trend (Aktivite Trendleri)

Kullanıcının zaman bazlı aktivite trendlerini içerir.

#### Haftalık Trend
- **weeklyTrend**: Son 12 haftalık haftalık aktivite trendi
  - `week`: Hafta formatı (YYYY-WW)
  - `commits`: Bu haftadaki commit sayısı
  - `pullRequests`: Bu haftadaki PR sayısı
  - `reviews`: Bu haftadaki review sayısı
  - `totalActivity`: Bu haftadaki toplam aktivite

#### Aylık Trend
- **monthlyTrend**: Son 6 aylık aylık aktivite trendi
  - `month`: Ay formatı (YYYY-MM)
  - `commits`: Bu aydaki commit sayısı
  - `pullRequests`: Bu aydaki PR sayısı
  - `reviews`: Bu aydaki review sayısı
  - `totalActivity`: Bu aydaki toplam aktivite

#### Son Aktivite
- **totalActivityLast30Days**: Son 30 gündeki toplam aktivite (commit + PR + review)
- **totalActivityLast7Days**: Son 7 gündeki toplam aktivite

#### Büyüme Metrikleri
- **activityGrowthRate**: Aktivite büyüme oranı yüzdesi (son 30 gün vs önceki 30 gün)
- **mostActiveWeek**: En aktif hafta (YYYY-WW formatında)
- **mostActiveMonth**: En aktif ay (YYYY-MM formatında)

#### Detaylı Dağılım
- **last30DaysBreakdown**: Son 30 günün detaylı aktivite dağılımı
  - `commits`: Commit sayısı
  - `pullRequests`: PR sayısı
  - `reviews`: Review sayısı
  - `commitsByDay`: Günlere göre commit dağılımı (Map: YYYY-MM-DD -> count)
  - `prsByDay`: Günlere göre PR dağılımı (Map: YYYY-MM-DD -> count)

**Kullanım:** Kullanıcının aktivite trendlerini, büyüme oranlarını ve zaman içindeki değişimini gösterir.

---

## 📝 Genel Notlar

1. **Kapsam:** Tüm metrikler kullanıcının sistemde kayıtlı olan TÜM repo'ları üzerinden hesaplanır.

2. **Yüzde Değerleri:** Tüm yüzde alanları 0-100 arası değerler içerir (örn: 84.62 = %84.62)

3. **Tarih Formatları:**
   - Hafta: `YYYY-WW` (örn: "2024-W01")
   - Ay: `YYYY-MM` (örn: "2024-01")
   - Gün: `YYYY-MM-DD` (örn: "2024-01-15")

4. **Saat Formatı:** `commitsByHour` içinde saat 0-23 arası değerler (0 = gece yarısı, 23 = 23:00)

5. **Haftanın Günleri:** `commitsByDayOfWeek` içinde İngilizce gün isimleri (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)

6. **Null Değerler:** Eğer veri yoksa, sayısal alanlar 0, yüzde alanları 0.00, listeler boş array, map'ler boş map döner.

7. **Hesaplama Mantığı:**
   - Tüm metrikler kullanıcının sahip olduğu tüm repo'lar üzerinden hesaplanır
   - Sadece mevcut veriler kullanılır (null olanlar hariç tutulur)
   - Ortalamalar BigDecimal ile hesaplanır (2-4 ondalık basamak)

8. **Performans:**
   - Büyük veri setleri için response süresi artabilir
   - `@Transactional(readOnly = true)` ile optimize edilmiştir
   - İlk çağrıda hesaplama yapılır, sonraki çağrılarda cache kullanılabilir (ileride eklenebilir)

