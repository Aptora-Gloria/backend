# Metrics veritabanı

## Veri kaynağı

Metrics modülü kendi tablosu **yoktur**. Hesaplamalar GitHub ve Auth modülü verileri üzerinden yapılır.

## Kullanılan tablolar

| Tablo | Kullanım |
|-------|----------|
| github_users | Kullanıcı–repo ilişkisi, login çözümleme |
| repositories | Repo listesi ve erişim |
| github_pull_requests | PR metrikleri (merge, boyut, sayı) |
| github_pull_request_reviews | Review istatistikleri |
| github_repository_commits | Commit metrikleri ve zaman analizi |
| github_branches | Branch metrikleri |
| github_repository_languages | Dil metrikleri ve çeşitlilik |
| github_pull_request_commits | PR–commit ilişkisi (kod kalitesi vb.) |

## Kısıtlar

Metrics tarafında ek DB kısıtı yok; okuma GitHub/Auth şemasına bağlıdır.
