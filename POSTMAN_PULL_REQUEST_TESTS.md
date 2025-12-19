# Postman Test İstekleri - Pull Request API

## Base URL
```
http://localhost:8080
```

## Authorization Header
Tüm isteklerde `Authorization` header'ı gereklidir:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## 🔄 PULL REQUEST ENDPOINT'LERİ

### 1. Repository'deki Tüm Pull Request'leri Getir
**Method:** `POST`  
**URL:** `http://localhost:8080/api/github/pull-requests/query`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "repoFullName": "owner/repository-name"
}
```

**Örnek Request:**
```
POST http://localhost:8080/api/github/pull-requests/query
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "repoFullName": "johndoe/my-awesome-project"
}
```

**Örnek Response:**
```json
[
  {
    "id": 1,
    "githubPrId": 123456789,
    "prNumber": 42,
    "repoFullName": "johndoe/my-awesome-project",
    "dataLevel": "FULL",
    "state": "open",
    "draft": false,
    "title": "Add new feature for user authentication",
    "body": "This PR implements OAuth2 authentication...",
    "authorLogin": "johndoe",
    "authorGithubId": 987654321,
    "htmlUrl": "https://github.com/johndoe/my-awesome-project/pull/42",
    "mergeCommitSha": null,
    "changeStats": {
      "additions": 150,
      "deletions": 25,
      "changedFiles": 8,
      "available": true,
      "syncedAt": "2024-01-20T14:30:00Z"
    },
    "createdAt": "2024-01-15T10:00:00Z",
    "updatedAt": "2024-01-20T14:30:00Z",
    "closedAt": null,
    "mergedAt": null,
    "lastSyncedAt": "2024-01-20T14:30:00Z"
  }
]
```

---

### 2. Belirli Bir Pull Request'i Getir
**Method:** `POST`  
**URL:** `http://localhost:8080/api/github/pull-requests/query/single`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "repoFullName": "owner/repository-name",
  "prNumber": 42
}
```

**Örnek Request:**
```
POST http://localhost:8080/api/github/pull-requests/query/single
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "repoFullName": "johndoe/my-awesome-project",
  "prNumber": 42
}
```

**Örnek Response:**
```json
{
  "id": 1,
  "githubPrId": 123456789,
  "prNumber": 42,
  "repoFullName": "johndoe/my-awesome-project",
  "dataLevel": "FULL",
  "state": "open",
  "draft": false,
  "title": "Add new feature for user authentication",
  "body": "This PR implements OAuth2 authentication with Spring Security...",
  "authorLogin": "johndoe",
  "authorGithubId": 987654321,
  "htmlUrl": "https://github.com/johndoe/my-awesome-project/pull/42",
  "mergeCommitSha": null,
  "changeStats": {
    "additions": 150,
    "deletions": 25,
    "changedFiles": 8,
    "available": true,
    "syncedAt": "2024-01-20T14:30:00Z"
  },
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-20T14:30:00Z",
  "closedAt": null,
  "mergedAt": null,
  "lastSyncedAt": "2024-01-20T14:30:00Z"
}
```

---

### 3. Pull Request Review'larını Getir
**Method:** `POST`  
**URL:** `http://localhost:8080/api/github/pull-requests/reviews`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "repoFullName": "owner/repository-name",
  "prNumber": 42
}
```

**Örnek Request:**
```
POST http://localhost:8080/api/github/pull-requests/reviews
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "repoFullName": "johndoe/my-awesome-project",
  "prNumber": 42
}
```

**Örnek Response:**
```json
[
  {
    "id": 1,
    "githubReviewId": 987654321,
    "pullRequestId": 1,
    "reviewer": {
      "login": "janesmith",
      "githubId": 123456789
    },
    "state": "APPROVED",
    "body": "Looks good! Great implementation of OAuth2.",
    "submittedAt": "2024-01-18T16:45:00Z",
    "htmlUrl": "https://github.com/johndoe/my-awesome-project/pull/42#pullrequestreview-987654321"
  },
  {
    "id": 2,
    "githubReviewId": 987654322,
    "pullRequestId": 1,
    "reviewer": {
      "login": "bobwilson",
      "githubId": 123456790
    },
    "state": "CHANGES_REQUESTED",
    "body": "Please add more unit tests for the authentication service.",
    "submittedAt": "2024-01-19T09:30:00Z",
    "htmlUrl": "https://github.com/johndoe/my-awesome-project/pull/42#pullrequestreview-987654322"
  }
]
```

---

### 4. Pull Request Commit'lerini Getir
**Method:** `POST`  
**URL:** `http://localhost:8080/api/github/pull-requests/commits`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "repoFullName": "owner/repository-name",
  "prNumber": 42
}
```

**Örnek Request:**
```
POST http://localhost:8080/api/github/pull-requests/commits
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "repoFullName": "johndoe/my-awesome-project",
  "prNumber": 42
}
```

**Örnek Response:**
```json
[
  {
    "id": 1,
    "githubRepoId": 123456789,
    "sha": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0",
    "pullRequestId": 1,
    "message": "Add OAuth2 configuration",
    "author": {
      "login": "johndoe",
      "name": "John Doe",
      "email": "john.doe@example.com"
    },
    "committedAt": "2024-01-15T10:30:00Z",
    "htmlUrl": "https://github.com/johndoe/my-awesome-project/commit/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0"
  },
  {
    "id": 2,
    "githubRepoId": 123456789,
    "sha": "b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0a1",
    "pullRequestId": 1,
    "message": "Add authentication service tests",
    "author": {
      "login": "johndoe",
      "name": "John Doe",
      "email": "john.doe@example.com"
    },
    "committedAt": "2024-01-16T14:20:00Z",
    "htmlUrl": "https://github.com/johndoe/my-awesome-project/commit/b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0a1"
  }
]
```

---

## 🔄 SYNCHRONIZATION ENDPOINT'LERİ

### 5. Pull Request Senkronizasyonunu Başlat
**Method:** `POST`  
**URL:** `http://localhost:8080/api/github/pull-requests/sync/initiate`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "repoFullName": "owner/repository-name"
}
```

**Örnek Request:**
```
POST http://localhost:8080/api/github/pull-requests/sync/initiate
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "repoFullName": "johndoe/my-awesome-project"
}
```

**Örnek Response:**
```
HTTP 200 OK
(Boş response body - işlem başarıyla başlatıldı)
```

---

### 6. Senkronizasyon Durumunu Kontrol Et
**Method:** `POST`  
**URL:** `http://localhost:8080/api/github/pull-requests/sync/status`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "repoFullName": "owner/repository-name"
}
```

**Örnek Request:**
```
POST http://localhost:8080/api/github/pull-requests/sync/status
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "repoFullName": "johndoe/my-awesome-project"
}
```

**Örnek Response:**
```json
{
  "repoFullName": "johndoe/my-awesome-project",
  "overallStatus": "COMPLETED",
  "jobs": [
    {
      "jobType": "PR_LIST",
      "status": "COMPLETED",
      "attemptCount": 1,
      "errorMessage": null,
      "startedAt": "2024-01-20T14:00:00Z",
      "completedAt": "2024-01-20T14:05:00Z",
      "nextRunAt": null
    },
    {
      "jobType": "PR_DETAIL",
      "status": "COMPLETED",
      "attemptCount": 1,
      "errorMessage": null,
      "startedAt": "2024-01-20T14:05:00Z",
      "completedAt": "2024-01-20T14:10:00Z",
      "nextRunAt": null
    },
    {
      "jobType": "PR_REVIEWS",
      "status": "RUNNING",
      "attemptCount": 1,
      "errorMessage": null,
      "startedAt": "2024-01-20T14:10:00Z",
      "completedAt": null,
      "nextRunAt": null
    },
    {
      "jobType": "PR_COMMITS",
      "status": "PENDING",
      "attemptCount": 0,
      "errorMessage": null,
      "startedAt": null,
      "completedAt": null,
      "nextRunAt": "2024-01-20T14:15:00Z"
    }
  ],
  "lastSyncedAt": "2024-01-20T14:10:00Z"
}
```

---

## 📋 Postman Collection Özeti

### Pull Request Data Endpoints
1. ✅ `POST /api/github/pull-requests/query` - Repository'deki tüm PR'ları getir
2. ✅ `POST /api/github/pull-requests/query/single` - Belirli bir PR'ı getir
3. ✅ `POST /api/github/pull-requests/reviews` - PR review'larını getir
4. ✅ `POST /api/github/pull-requests/commits` - PR commit'lerini getir

### Synchronization Endpoints
5. ✅ `POST /api/github/pull-requests/sync/initiate` - Senkronizasyonu başlat
6. ✅ `POST /api/github/pull-requests/sync/status` - Senkronizasyon durumunu kontrol et

---

## 🔑 Önemli Notlar

1. **JWT Token:** `Authorization` header'ında `Bearer {token}` formatında gönderilmelidir.
2. **Repository Full Name:** `owner/repository-name` formatında olmalıdır (örn: `johndoe/my-project`).
3. **Data Level:** `BASIC` (sadece temel PR bilgileri) veya `FULL` (tüm detaylar dahil) olabilir.
4. **Change Stats:** `available: true` ise istatistikler mevcut, `false` ise henüz çekilmemiş.
5. **Sync Jobs:** Sıralı olarak çalışır: PR_LIST → PR_DETAIL → PR_REVIEWS → PR_COMMITS.

---

## 🧪 Test Senaryosu Örneği

### Temel Test Akışı:
1. **Senkronizasyonu başlat:**
   ```
   POST /api/github/pull-requests/sync/initiate
   Body: { "repoFullName": "johndoe/my-project" }
   ```

2. **Durumu kontrol et:**
   ```
   POST /api/github/pull-requests/sync/status
   Body: { "repoFullName": "johndoe/my-project" }
   ```

3. **PR'ları listele:**
   ```
   POST /api/github/pull-requests/query
   Body: { "repoFullName": "johndoe/my-project" }
   ```

4. **Belirli bir PR'ı getir:**
   ```
   POST /api/github/pull-requests/query/single
   Body: { "repoFullName": "johndoe/my-project", "prNumber": 42 }
   ```

5. **PR review'larını kontrol et:**
   ```
   POST /api/github/pull-requests/reviews
   Body: { "repoFullName": "johndoe/my-project", "prNumber": 42 }
   ```

6. **PR commit'lerini kontrol et:**
   ```
   POST /api/github/pull-requests/commits
   Body: { "repoFullName": "johndoe/my-project", "prNumber": 42 }
   ```

---

## ⚠️ Hata Durumları

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid"
}
```

### 404 Not Found
```json
{
  "error": "Repository Not Found",
  "message": "Repository 'johndoe/my-project' not found or not accessible"
}
```

### 400 Bad Request
```json
{
  "error": "Validation Error",
  "message": "repoFullName is required"
}
```

---

## 🚀 Postman Collection İçe Aktarma

Bu endpoint'leri Postman'e kolayca eklemek için:

1. Postman'i açın
2. "Import" butonuna tıklayın
3. Bu dosyayı seçin veya endpoint'leri manuel olarak ekleyin
4. Environment variable'ları ayarlayın:
   - `baseUrl`: `http://localhost:8080`
   - `authToken`: JWT token'ınız
5. Her request için `Authorization: Bearer {{authToken}}` header'ını ekleyin

Bu şekilde tüm Pull Request API'lerini kolayca test edebilirsiniz! 🎉