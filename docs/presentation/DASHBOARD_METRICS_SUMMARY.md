# Dashboard — Metrik Özeti (Sunum Notu)

**Hedef:** 4 dakikada ana mesajı vermek. Her şeyi anlatma; tek ana odak (ör. PR’lar veya commit aktivitesi).

---

## Ana söylenecek (giriş)

> “Dashboard’da gördüğümüz tüm metrikler GitHub’daki tek bir kaynaktan geliyor: commit’ler, pull request’ler ve review’lar. Örneğin burada gördüğümüz PR metrikleri: kaç PR merge edilmiş, ne kadar sürede merge edilmiş, ne kadar büyük olduğu gibi bilgileri özetliyor.”

**Net mesaj:** “Amaç grafik göstermek değil, repo’nun geliştirme süreci hakkında hızlı bir fikir vermek.”

---

## Repository Health — Nasıl oluşuyor? (Tek sayfa özet)

Repository Health, **dört boyutun ağırlıklı ortalaması** ile tek bir 0–100 skor ve durum etiketi (EXCELLENT / GOOD / FAIR / POOR / CRITICAL) olarak hesaplanıyor. Backend: `HealthScoreCalculator`, `HealthScoreInput` → `HealthScoreResult`; API: `POST /api/metrics/repository/health`.

| Boyut | Ağırlık | Ne ile hesaplanıyor? |
|-------|--------|----------------------|
| **ACTIVITY** | %30 | Son 30 gündeki toplam aktivite (commit/PR vb.). Eşikler: 10+ → 40, 20+ → 60, 50+ → 80, 100+ → 100. |
| **QUALITY** | %30 | Kod kalitesi skoru (net kod değişimi, büyüme oranı, ortalama commit mesajı, düzenli commit oranı vb. ile hesaplanan overall quality). |
| **COMMUNITY** | %20 | Katkıda bulunan sayısı + son 30 günde aktif olanlar. Örn. 1 kişi → 20, 2+ → 40, 3+ ve 2 aktif → 60, 5+ ve 3 aktif → 80, 10+ ve 5 aktif → 100. |
| **POPULARITY** | %20 | stars + forks + watchers toplamı. Örn. 10+ → 20, 50+ → 40, 100+ → 60, 500+ → 80, 1000+ → 100. |

**Genel skor:** `0.3×ACTIVITY + 0.2×POPULARITY + 0.3×QUALITY + 0.2×COMMUNITY` (yuvarlanmış).  
**Durum:** ≥80 EXCELLENT, ≥60 GOOD, ≥40 FAIR, ≥20 POOR, &lt;20 CRITICAL.

*Kanıt: `metrics.domain.service.HealthScoreCalculator`, `HealthScoreInput`; veri `RepositoryMetricsServiceAdapter.getHealthScoreInput` ile activity trend, overview, code quality, contributor metriklerinden toplanıyor.*

---

## Diğer metrikler — Tek cümle açıklama

| Metrik | Tek cümle |
|--------|-----------|
| **Activity Trend** | Seçilen dönemde (7d/30d) repo geliştirme yoğunluğunun zaman içinde nasıl değiştiğini gösterir. |
| **Daily Rhythm** | Haftanın hangi günlerinde daha çok geliştirme yapıldığını (ör. hafta ortası yoğun) gösterir. |
| **Work Balance** | İşin hafta içi / hafta sonu dağılımını özetler (“çoğunlukla hafta içi” vb.). |
| **Productivity Hours** | Günün hangi saatlerinde commit/aktivite yoğunluğunun arttığını gösterir. |
| **Code Velocity** | Kod tabanındaki net satır değişimi ve büyüme oranı; geliştirme hızının özeti. |
| **Review Insights** | PR başına ortalama review sayısı, review yapılmamış PR oranı ve onay oranını özetler. |
| **Review Status** | Toplam review’ların dağılımı: yorum, değişiklik isteği, onay; merkezde onay yüzdesi. |
| **PR Sizes** | PR’ları küçük/orta/büyük kategorilere ayırır; merge oranı ve ortalama merge süresi ile birlikte sunar. |
| **Top Languages** | Repodaki dil dağılımı; hangi dillerin ne oranda kullanıldığını gösterir. |
| **Quality Gates** | Net kod değişimi, büyüme oranı, genel kalite skoru, ortalama commit mesajı uzunluğu, düzenli commit oranı gibi kalite göstergelerinin özeti. |
| **Top Contributors** | En çok commit yapan katkıda bulunanları listeler. |
| **Top Reviewers** | En çok review yapan kullanıcıları ve review sayısı / oranını gösterir. |
| **Repository Details** | Forks, watchers, stars, open issues, repo yaşı, son sync zamanı gibi repo özet bilgileri. |

---

## Sunumda odaklanılacak tek ana metrik (örnek)

- **PR odaklı:** “Merge oranı, ortalama merge süresi ve PR boyut dağılımı — hepsi GitHub’daki PR ve review verisinden; amacımız süreç hakkında hızlı fikir.”
- **Commit odaklı:** “Activity trend ve productivity hours — commit zamanlarından; repo ne kadar aktif, günün hangi saatlerinde yoğun, tek bakışta görüyoruz.”

Bu doküman, demo sırasında “Repository Health nasıl oluşuyor?” ve “Bu kart ne?” sorularına tek sayfadan cevap vermek içindir.
