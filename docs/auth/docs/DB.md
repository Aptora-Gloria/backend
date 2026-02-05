# Auth veritabanı

## Tablo: github_users

Kullanıcı bilgisi ve GitHub access token saklanır.

| Kolon | Tip | Nullable | Açıklama |
|-------|-----|----------|----------|
| id | BIGSERIAL | Hayır | PK |
| github_user_id | BIGINT | Hayır | GitHub kullanıcı id (UNIQUE) |
| login | VARCHAR(100) | Hayır | GitHub login |
| login_ci | VARCHAR(100) | Hayır | Case-insensitive arama için |
| access_token | TEXT | - | GitHub OAuth access token |
| name | VARCHAR(200) | Evet | Görünen ad |
| avatar_url | VARCHAR(500) | Evet | Avatar URL |
| html_url | VARCHAR(500) | Evet | Profil URL |
| owner_type | VARCHAR(16) | Hayır | USER / ORG |
| created_at | TIMESTAMPTZ | Hayır | Oluşturulma |
| updated_at | TIMESTAMPTZ | Hayır | Güncellenme |
| last_synced_at | TIMESTAMPTZ | Evet | Son sync zamanı |

## Kısıtlar

- `github_user_id` UNIQUE.
- `login_ci` ile case-insensitive login araması yapılır.
