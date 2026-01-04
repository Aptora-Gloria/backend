# Language Service - Postman Test URL'leri

## Base URL
```
http://localhost:8080/api/github
```

## 1. POST - Initialize Languages (GitHub API'den çek ve kaydet)

**Method:** `POST`  
**URL:** `http://localhost:8080/api/github/languages/init`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body (JSON):**
```json
{
  "repoFullName": "owner/repo-name"
}
```

**Örnek:**
```json
{
  "repoFullName": "facebook/react"
}
```

---

## 2. POST - Get Languages from Database (Raw Format)

**Method:** `POST`  
**URL:** `http://localhost:8080/api/github/languages/query`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body (JSON):**
```json
{
  "repoFullName": "owner/repo-name"
}
```

**Örnek:**
```json
{
  "repoFullName": "facebook/react"
}
```

---

## 3. POST - Get Languages for UI (Percentage Format)

**Method:** `POST`  
**URL:** `http://localhost:8080/api/github/languages/query/ui`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body (JSON):**
```json
{
  "repoFullName": "owner/repo-name"
}
```

**Örnek:**
```json
{
  "repoFullName": "facebook/react"
}
```

---

## Postman Collection Örneği

### Collection Variables:
```
baseUrl: http://localhost:8080
jwtToken: <YOUR_JWT_TOKEN>
repoFullName: facebook/react
```

### Request Examples:

#### 1. Init Languages
```
POST {{baseUrl}}/api/github/languages/init
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 2. Get Languages (Raw)
```
POST {{baseUrl}}/api/github/languages/query
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 3. Get Languages (UI)
```
POST {{baseUrl}}/api/github/languages/query/ui
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

---

## Notlar

1. **Authentication:** `@AuthenticationPrincipal` kullanıldığı için POST endpoint'i için JWT token gerekli olabilir. GET endpoint'leri için authentication gerekip gerekmediğini kontrol edin.

2. **URL Encoding:** Postman genellikle path variable'ları otomatik encode eder. Eğer sorun yaşarsanız, `/` karakterini `%2F` ile değiştirin.

3. **Fullname Format:** `owner/repo-name` formatında olmalı (örn: `facebook/react`, `microsoft/vscode`)

4. **Test Sırası:**
   - Önce POST ile init yapın (GitHub API'den çeker ve DB'ye kaydeder)
   - Sonra GET endpoint'leri ile veritabanından okuyun

5. **Request Body:** Tüm endpoint'ler POST method kullanıyor ve request body ile çalışıyor (commit servisi gibi).

