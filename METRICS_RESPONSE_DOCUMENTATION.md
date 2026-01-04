# Metrics Response Alanları Açıklaması

## 📊 1. PrMergeRateDto (PR Merge Oranı Metrikleri)

| Alan | Açıklama |
|------|----------|
| `totalPullRequests` | Kullanıcının tüm repo'larındaki toplam PR sayısı |
| `mergedPullRequests` | Merge edilmiş PR sayısı |
| `openPullRequests` | Açık (open) durumundaki PR sayısı |
| `closedPullRequests` | Kapatılmış ama merge edilmemiş PR sayısı |
| `mergeRate` | Merge oranı yüzdesi (0-100). `mergedPullRequests / totalPullRequests * 100` |
| `averageMergeTimeHours` | PR'ın oluşturulmasından merge edilmesine kadar geçen ortalama süre (saat cinsinden) |

---

## 📏 2. PrSizeDto (PR Boyut Metrikleri)

| Alan | Açıklama |
|------|----------|
| `averageAdditions` | Ortalama eklenen satır sayısı (tüm PR'lar için) |
| `averageDeletions` | Ortalama silinen satır sayısı |
| `averageChangedFiles` | Ortalama değiştirilen dosya sayısı |
| `averageTotalChanges` | Ortalama toplam değişiklik (additions + deletions) |
| `smallPrCount` | Küçük PR sayısı (< 100 satır) |
| `mediumPrCount` | Orta PR sayısı (100-500 satır arası) |
| `largePrCount` | Büyük PR sayısı (> 500 satır) |
| `smallPrPercentage` | Küçük PR'ların yüzdesi |
| `mediumPrPercentage` | Orta PR'ların yüzdesi |
| `largePrPercentage` | Büyük PR'ların yüzdesi |

**Not:** PR boyutu = additions + deletions

---

## 👀 3. ReviewStatsDto (Review İstatistikleri)

| Alan | Açıklama |
|------|----------|
| `totalReviews` | Toplam review sayısı (tüm PR'lar için) |
| `approvedReviews` | Onaylanmış (APPROVED) review sayısı |
| `changesRequestedReviews` | Değişiklik istenen (CHANGES_REQUESTED) review sayısı |
| `commentedReviews` | Sadece yorum yapılan (COMMENTED) review sayısı |
| `averageReviewsPerPr` | PR başına ortalama review sayısı |
| `approvalRate` | Onay oranı yüzdesi (0-100). `approvedReviews / totalReviews * 100` |
| `reviewsByState` | State'e göre review dağılımı (Map: state -> count) |
| `prsWithoutReviews` | Review almayan PR sayısı |
| `prsWithReviewsPercentage` | Review alan PR'ların yüzdesi |

---

## ⏰ 4. CommitTimeAnalysisDto (Commit Zaman Analizi)

| Alan | Açıklama |
|------|----------|
| `commitsByHour` | Saate göre commit dağılımı (Map: hour 0-23 -> count) |
| `commitsByDayOfWeek` | Haftanın gününe göre commit dağılımı (MONDAY, TUESDAY, vb.) |
| `weekdayCommits` | Hafta içi (Pazartesi-Cuma) commit sayısı |
| `weekendCommits` | Hafta sonu (Cumartesi-Pazar) commit sayısı |
| `weekdayPercentage` | Hafta içi commit yüzdesi |
| `weekendPercentage` | Hafta sonu commit yüzdesi |
| `mostActiveHour` | En aktif saat (0-23 arası) |
| `mostActiveDay` | En aktif gün (MONDAY, TUESDAY, vb.) |
| `last30DaysCommits` | Son 30 gündeki commit sayısı |
| `last7DaysCommits` | Son 7 gündeki commit sayısı |

---

## 🌐 5. LanguageDiversityDto (Dil Çeşitliliği)

| Alan | Açıklama |
|------|----------|
| `totalLanguages` | Toplam farklı dil sayısı |
| `uniqueLanguagesAcrossRepos` | Tüm repo'lar genelinde kullanılan benzersiz dil sayısı |
| `averageLanguagesPerRepo` | Repo başına ortalama dil sayısı |
| `languageUsageCount` | Her dilin kaç repo'da kullanıldığı (Map: language -> repo count) |
| `languagePercentageDistribution` | Her dilin toplam kod içindeki yüzdesi (Map: language -> percentage) |
| `topLanguages` | En çok kullanılan diller listesi (LanguageUsageDto array) |
| `singleLanguageRepos` | Tek dil kullanan repo sayısı |
| `multiLanguageRepos` | Çok dilli (2+ dil) repo sayısı |
| `multiLanguageReposPercentage` | Çok dilli repo'ların yüzdesi |

### LanguageUsageDto (topLanguages içinde)
| Alan | Açıklama |
|------|----------|
| `language` | Dil adı (örn: Java, TypeScript) |
| `repoCount` | Bu dili kullanan repo sayısı |
| `totalPercentage` | Bu dilin toplam kod içindeki yüzdesi |
| `totalBytes` | Bu dilin toplam byte miktarı |

---

## 📈 6. ActivityTrendDto (Aktivite Trendleri)

| Alan | Açıklama |
|------|----------|
| `weeklyTrend` | Son 12 haftalık haftalık aktivite trendi (WeeklyActivityDto array) |
| `monthlyTrend` | Son 6 aylık aylık aktivite trendi (MonthlyActivityDto array) |
| `totalActivityLast30Days` | Son 30 gündeki toplam aktivite (commit + PR + review) |
| `totalActivityLast7Days` | Son 7 gündeki toplam aktivite |
| `activityGrowthRate` | Aktivite büyüme oranı yüzdesi (son 30 gün vs önceki 30 gün) |
| `mostActiveWeek` | En aktif hafta (YYYY-WW formatında) |
| `mostActiveMonth` | En aktif ay (YYYY-MM formatında) |
| `last30DaysBreakdown` | Son 30 günün detaylı aktivite dağılımı (ActivityBreakdownDto) |

### WeeklyActivityDto (weeklyTrend içinde)
| Alan | Açıklama |
|------|----------|
| `week` | Hafta formatı (YYYY-WW, örn: "2024-W01") |
| `commits` | Bu haftadaki commit sayısı |
| `pullRequests` | Bu haftadaki PR sayısı |
| `reviews` | Bu haftadaki review sayısı |
| `totalActivity` | Bu haftadaki toplam aktivite (commits + PRs + reviews) |

### MonthlyActivityDto (monthlyTrend içinde)
| Alan | Açıklama |
|------|----------|
| `month` | Ay formatı (YYYY-MM, örn: "2024-01") |
| `commits` | Bu aydaki commit sayısı |
| `pullRequests` | Bu aydaki PR sayısı |
| `reviews` | Bu aydaki review sayısı |
| `totalActivity` | Bu aydaki toplam aktivite |

### ActivityBreakdownDto (last30DaysBreakdown içinde)
| Alan | Açıklama |
|------|----------|
| `commits` | Son 30 gündeki commit sayısı |
| `pullRequests` | Son 30 gündeki PR sayısı |
| `reviews` | Son 30 gündeki review sayısı |
| `commitsByDay` | Günlere göre commit dağılımı (Map: YYYY-MM-DD -> count) |
| `prsByDay` | Günlere göre PR dağılımı (Map: YYYY-MM-DD -> count) |

---

## 🎯 7. UserMetricsDto (Tüm Metrikler)

| Alan | Açıklama |
|------|----------|
| `prMergeRate` | PR merge oranı metrikleri (PrMergeRateDto) |
| `prSize` | PR boyut metrikleri (PrSizeDto) |
| `reviewStats` | Review istatistikleri (ReviewStatsDto) |
| `commitTimeAnalysis` | Commit zaman analizi (CommitTimeAnalysisDto) |
| `languageDiversity` | Dil çeşitliliği (LanguageDiversityDto) |
| `activityTrend` | Aktivite trendleri (ActivityTrendDto) |

**Not:** Bu DTO, yukarıdaki tüm metrikleri tek bir response'da birleştirir.

---

## 📝 Genel Notlar

1. **Yüzde Değerleri:** Tüm yüzde alanları 0-100 arası değerler içerir (örn: 84.62 = %84.62)

2. **Tarih Formatları:**
   - Hafta: `YYYY-WW` (örn: "2024-W01")
   - Ay: `YYYY-MM` (örn: "2024-01")
   - Gün: `YYYY-MM-DD` (örn: "2024-01-15")

3. **Saat Formatı:** `commitsByHour` içinde saat 0-23 arası değerler (0 = gece yarısı, 23 = 23:00)

4. **Haftanın Günleri:** `commitsByDayOfWeek` içinde İngilizce gün isimleri (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)

5. **Null Değerler:** Eğer veri yoksa, sayısal alanlar 0, yüzde alanları 0.00, listeler boş array, map'ler boş map döner.

6. **Hesaplama Mantığı:**
   - Tüm metrikler kullanıcının sahip olduğu tüm repo'lar üzerinden hesaplanır
   - Sadece mevcut veriler kullanılır (null olanlar hariç tutulur)
   - Ortalamalar BigDecimal ile hesaplanır (2 ondalık basamak)

