# Metrics Module Documentation

Bu klasör, Optimetric Metrics modülü ile ilgili tüm dokümantasyonu içerir.

## 📚 Dokümantasyon İçeriği

### User Metrikleri

- **[USER_METRICS_EXPLANATION.md](./USER_METRICS_EXPLANATION.md)** - User metriklerinin detaylı açıklaması
- **[USER_METRICS_PROPOSAL.md](./USER_METRICS_PROPOSAL.md)** - User metrikleri önerisi ve analizi
- **[USER_METRICS_REPO_SCOPE_ANALYSIS.md](./USER_METRICS_REPO_SCOPE_ANALYSIS.md)** - User metriklerinde repo kapsamı analizi
- **[METRICS_POSTMAN_TEST_URLS.md](./METRICS_POSTMAN_TEST_URLS.md)** - User metrikleri için Postman test URL'leri
- **[METRICS_RESPONSE_DOCUMENTATION.md](./METRICS_RESPONSE_DOCUMENTATION.md)** - User metrikleri response alanları açıklaması

### Repository Metrikleri

- **[REPOSITORY_METRICS_EXPLANATION.md](./REPOSITORY_METRICS_EXPLANATION.md)** - Repository metriklerinin detaylı açıklaması
- **[REPO_METRICS_ANALYSIS.md](./REPO_METRICS_ANALYSIS.md)** - Repository metrikleri analizi ve önerileri
- **[REPOSITORY_METRICS_POSTMAN_TEST_URLS.md](./REPOSITORY_METRICS_POSTMAN_TEST_URLS.md)** - Repository metrikleri için Postman test URL'leri

### Karşılaştırma ve Analiz

- **[USER_VS_REPO_METRICS_ANALYSIS.md](./USER_VS_REPO_METRICS_ANALYSIS.md)** - User vs Repository metrikleri karşılaştırması

### Diğer Dokümantasyon

- **[POSTMAN_TEST_URLS.md](./POSTMAN_TEST_URLS.md)** - Language Service için Postman test URL'leri
- **[COMMIT_MESSAGE.md](./COMMIT_MESSAGE.md)** - Commit mesaj örnekleri
- **[HEXAGONAL_ARCHITECTURE_REFACTOR_PLAN.md](./HEXAGONAL_ARCHITECTURE_REFACTOR_PLAN.md)** - Hexagonal Architecture refactor planı

---

## 🚀 Hızlı Başlangıç

### User Metrikleri

1. [User Metrikleri Açıklaması](./USER_METRICS_EXPLANATION.md) - Metriklerin ne anlama geldiğini öğrenin
2. [Postman Test URL'leri](./METRICS_POSTMAN_TEST_URLS.md) - API'yi test edin

### Repository Metrikleri

1. [Repository Metrikleri Açıklaması](./REPOSITORY_METRICS_EXPLANATION.md) - Metriklerin ne anlama geldiğini öğrenin
2. [Postman Test URL'leri](./REPOSITORY_METRICS_POSTMAN_TEST_URLS.md) - API'yi test edin

---

## 📖 Dokümantasyon Yapısı

```
docs/
├── README.md (bu dosya)
├── USER_METRICS_EXPLANATION.md
├── USER_METRICS_PROPOSAL.md
├── USER_METRICS_REPO_SCOPE_ANALYSIS.md
├── METRICS_POSTMAN_TEST_URLS.md
├── METRICS_RESPONSE_DOCUMENTATION.md
├── REPOSITORY_METRICS_EXPLANATION.md
├── REPO_METRICS_ANALYSIS.md
├── REPOSITORY_METRICS_POSTMAN_TEST_URLS.md
├── USER_VS_REPO_METRICS_ANALYSIS.md
├── POSTMAN_TEST_URLS.md
├── COMMIT_MESSAGE.md
└── HEXAGONAL_ARCHITECTURE_REFACTOR_PLAN.md
```

---

## 🔗 API Endpoint'leri

### User Metrikleri
- `POST /api/metrics/user` - Tüm user metrikleri
- `POST /api/metrics/pr-merge-rate` - PR merge oranı
- `POST /api/metrics/pr-size` - PR boyutu
- `POST /api/metrics/review-stats` - Review istatistikleri
- `POST /api/metrics/commit-time-analysis` - Commit zaman analizi
- `POST /api/metrics/language-diversity` - Dil çeşitliliği
- `POST /api/metrics/activity-trend` - Aktivite trendleri

### Repository Metrikleri
- `POST /api/metrics/repository` - Tüm repository metrikleri
- `POST /api/metrics/repository/overview` - Genel bakış
- `POST /api/metrics/repository/pull-requests` - PR metrikleri
- `POST /api/metrics/repository/commits` - Commit metrikleri
- `POST /api/metrics/repository/reviews` - Review metrikleri
- `POST /api/metrics/repository/branches` - Branch metrikleri
- `POST /api/metrics/repository/languages` - Dil metrikleri
- `POST /api/metrics/repository/activity` - Aktivite trendleri
- `POST /api/metrics/repository/code-quality` - Kod kalitesi
- `POST /api/metrics/repository/contributors` - Contributor metrikleri
- `POST /api/metrics/repository/health` - Sağlık skoru

---

## 📝 Notlar

- Tüm endpoint'ler POST method kullanır
- Request body ile parametreler gönderilir
- JWT token authentication gerekli
- Validation: Tüm request alanları `@NotBlank` ile validate edilir

