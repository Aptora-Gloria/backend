# Metrics API

## Endpoint özeti

Tüm endpoint’ler JWT ile yetkilendirilir. Request body’de genelde `login` (user metrikleri) veya `repoFullName` (repo metrikleri) kullanılır.

### User metrikleri (`/api/metrics`)

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/api/metrics/user` | Tüm user metrikleri |
| POST | `/api/metrics/pr-merge-rate` | PR merge oranı |
| POST | `/api/metrics/pr-size` | PR boyutu |
| POST | `/api/metrics/review-stats` | Review istatistikleri |
| POST | `/api/metrics/commit-time-analysis` | Commit zaman analizi |
| POST | `/api/metrics/language-diversity` | Dil çeşitliliği |
| POST | `/api/metrics/activity-trend` | Aktivite trendleri |

### Repository metrikleri (`/api/metrics/repository`)

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/api/metrics/repository` | Tüm repo metrikleri |
| POST | `/api/metrics/repository/overview` | Genel bakış |
| POST | `/api/metrics/repository/pull-requests` | PR metrikleri |
| POST | `/api/metrics/repository/commits` | Commit metrikleri |
| POST | `/api/metrics/repository/reviews` | Review metrikleri |
| POST | `/api/metrics/repository/branches` | Branch metrikleri |
| POST | `/api/metrics/repository/languages` | Dil metrikleri |
| POST | `/api/metrics/repository/activity` | Aktivite trendi |
| POST | `/api/metrics/repository/code-quality` | Kod kalitesi |
| POST | `/api/metrics/repository/contributors` | Contributor metrikleri |
| POST | `/api/metrics/repository/health` | Sağlık skoru |

## Contract notu

Request: `GetUserMetricsRequest` (login), `GetRepositoryMetricsRequest` (repoFullName). Response tipleri `api.response` paketinde (UserMetricsDto, RepositoryMetricsDto, PrMergeRateDto, vb.). Detay için METRICS_RESPONSE_DOCUMENTATION.md ve REPOSITORY_METRICS_EXPLANATION.md kullanılabilir.
