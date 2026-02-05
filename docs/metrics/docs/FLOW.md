# Metrics servis akışı

## User metrikleri

1. İstemci `POST /api/metrics/...` ile hedeflenen metrik ve `login` gönderir.
2. **MetricsController** ilgili port’u çağırır (GetPrMergeRatePort, GetActivityTrendPort, vb.).
3. Use case / adapter GitHub modülü ve persistence’dan ham veriyi (PR, commit, review, language) okur.
4. Domain calculator’lar (PrMetricsCalculator, ActivityTrendCalculator, vb.) metrikleri hesaplar.
5. Sonuç API mapper ile DTO’ya çevrilip döner.

## Repository metrikleri

1. İstemci `POST /api/metrics/repository/...` ile `repoFullName` gönderir.
2. **RepositoryMetricsController** ilgili port’u çağırır (GetOverviewPort, GetCommitMetricsPort, vb.).
3. **RepositoryMetricsServiceAdapter** (ve ilgili port’lar) repo erişimini doğrular, GitHub/persistence’dan veri okur.
4. Domain calculator’lar overview, branch, commit, review, dil, kod kalitesi, health score, contributor metriklerini hesaplar.
5. Sonuç API mapper ile DTO’ya çevrilip döner.

## Bağımlılıklar

- **Giriş:** Auth (login), GitHub modülü (repo, PR, commit, branch, language, review verisi).
- **Çıkış:** Sadece okuma; persistence adapter’lar GitHub modülü JPA repository’lerini kullanır.
