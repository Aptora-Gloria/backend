# Repo Sync Modülü — Postman Test

**Base URL:** `http://localhost:8080`  
**Kimlik doğrulama:** JWT Bearer token (önce `POST /auth/login` ile alınır).

---

## 1. Token alma

Repo Sync endpoint'leri kimlik doğrulama gerektirir. Önce login ile token alın:

- **Method:** `POST`
- **URL:** `http://localhost:8080/auth/login`
- **Body (JSON):** `{ "code": "<GitHub_OAuth_code>" }`
- **Response:** `{ "accessToken": "<JWT>" }`

Postman'da sonraki isteklerde **Authorization** sekmesinde **Bearer Token** seçip bu `accessToken` değerini girin.

---

## 2. Sync tetikleme — POST /api/reposync/trigger

Seçilen repo için sync tetikler; eksikse job'ları kuyruğa alır.

### İstek

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/reposync/trigger`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer <JWT>`
- **Body (raw JSON):**

```json
{
  "fullName": "owner/repo"
}
```

`owner/repo` yerine gerçek bir repo tam adı yazın (örn. kullanıcının erişebildiği bir repo).

### Beklenen yanıtlar

**200 OK — Sync tetiklendi:**

```json
{
  "status": "TRIGGERED",
  "message": "Sync job'ları kuyruğa alındı."
}
```

**200 OK — Repo zaten hazır:**

```json
{
  "status": "ALREADY_READY",
  "message": "Repo zaten hazır."
}
```

**200 OK — Sync zaten devam ediyor:**

```json
{
  "status": "SYNC_ALREADY_RUNNING",
  "message": "Sync zaten devam ediyor."
}
```

**404 Not Found — Repo bulunamadı:**

- Body boş veya hata mesajı; repo veritabanında yoksa veya fullName yanlışsa.

**400 Bad Request — Validasyon hatası:**

- `fullName` boş veya gönderilmediyse.

---

## 3. Hazırlık sorgulama — POST /api/reposync/readiness

Seçilen repo için hazırlık durumunu döndürür (state, missing, flags).

### İstek

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/reposync/readiness`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer <JWT>`
- **Body (raw JSON):**

```json
{
  "fullName": "owner/repo"
}
```

### Beklenen yanıtlar

**200 OK:**

```json
{
  "state": "PARTIAL",
  "missing": ["COMMITS", "LANGUAGES"],
  "flags": {
    "branchesReady": true,
    "commitsReady": false,
    "pullRequestsReady": true,
    "prDetailsReady": true,
    "reviewsReady": true,
    "languagesReady": false
  }
}
```

- `state`: `READY` | `PARTIAL` | `NOT_READY`
- `missing`: Eksik bileşen adları (örn. BRANCHES, PRS, COMMITS, LANGUAGES)
- `flags`: Her bileşen için hazır mı (boolean)

**404 Not Found:** Repo bulunamadı (fullName yanlış veya repo DB'de yok).

**400 Bad Request:** `fullName` boş veya gönderilmediyse.

---

## 4. Test sırası (kısa)

1. `POST /auth/login` ile JWT al.
2. `POST /api/reposync/trigger` ile `fullName: "owner/repo"` gönder; 200 ve status TRIGGERED / ALREADY_READY / SYNC_ALREADY_RUNNING beklenir.
3. `POST /api/reposync/readiness` ile aynı `fullName` gönder; 200 ve state/missing/flags beklenir.
4. Var olmayan bir repo ile trigger/readiness dene; 404 beklenir.

---

## 5. Sorun giderme

**403 Forbidden alıyorsanız:**

1. **ComponentScan:** `BackendApplication` içinde `@ComponentScan(basePackages = ...)` listesinde `com.optimetric.github` ve `com.optimetric.shared` olduğundan emin olun (reposync API artık github modülü içindedir). Yoksa RepoSync controller yüklenmez ve istek kimlik doğrulama aşamasında 403 döner.
2. **Uygulamayı yeniden başlatın:** ComponentScan değişikliği sonrası mutlaka `mvn spring-boot:run` (veya IDE run) ile yeniden başlatın.
3. **Token bu istekte gidiyor mu:** Postman’da ilgili isteğin **Authorization** sekmesinde **Bearer Token** seçili ve token değeri dolu olsun. Collection/folder auth’a güvenmek yerine bu request’e özel token tanımlayın; **Headers** sekmesinde `Authorization: Bearer <JWT>` satırının göründüğünü kontrol edin.

**404 Not Found (repo bulunamadı):** `fullName` veritabanında kullanıcıya ait bir repo ile eşleşmiyorsa 404 döner. Önce `POST /api/github/repos` ile repoları çekip oradaki bir `fullName` kullanın.

---

## 6. cURL örnekleri

```bash
# Token al (code değerini GitHub OAuth callback'ten alın)
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"code":"<GITHUB_OAUTH_CODE>"}' | jq -r '.accessToken')

# Trigger
curl -s -X POST http://localhost:8080/api/reposync/trigger \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"fullName":"owner/repo"}'

# Readiness
curl -s -X POST http://localhost:8080/api/reposync/readiness \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"fullName":"owner/repo"}'
```
