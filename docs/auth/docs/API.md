# Auth API

## Endpoint'ler

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/auth/login` | GitHub OAuth kodu ile giriş; JWT döner |
| GET | `/auth/me` | Bearer token ile mevcut kullanıcı bilgisi |

## Contract

### POST /auth/login

**Request body:** `AuthRequest`

| Alan | Tip | Zorunlu | Açıklama |
|------|-----|---------|----------|
| code | string | Evet | GitHub OAuth authorization code |

**Response:** `AuthResponse` (200)

| Alan | Tip | Açıklama |
|------|-----|----------|
| accessToken | string | JWT access token |

**Hata:** 4xx/5xx (GitHub veya doğrulama hataları).

### GET /auth/me

**Header:** `Authorization: Bearer <jwt>`

**Response:** `GitHubUserDto` (200). Token yok veya geçersiz ise 401.

| Alan | Tip | Açıklama |
|------|-----|----------|
| id | Long | İç kullanıcı id |
| githubUserId | Long | GitHub kullanıcı id |
| login | string | GitHub login |
| name | string | Görünen ad |
| avatar_url | string | Avatar URL |
| htmlUrl | string | GitHub profil URL |
| ownerType | string | USER / ORG |
