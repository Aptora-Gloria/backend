# GitHub API Endpoints - Kısa Referans

## Base URL
```
http://localhost:8080/api/github
```

## Authorization
Tüm endpoint'lerde `Authorization` header'ı zorunludur:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## REPO ENDPOINT'LERİ

### 1. Kullanıcı Repo'larını GitHub'dan Çek ve Kaydet
**Method:** `POST`  
**URL:** `/repos/init/user`  
**Headers:** `Authorization` (Bearer token)

**Request:** Body yok

**Response:** `List<RepoInitrequest>`

---

### 2. Kullanıcı Repo'larını Getir
**Method:** `POST`  
**URL:** `/repos/query`  
**Headers:** `Authorization` (Bearer token)

**Request Body:**
```json
{
  "ownerType": "USER",  // Opsiyonel: USER veya ORGANIZATION
  "ownerLogin": "octocat"  // Opsiyonel: Owner login adı
}
```

**Response:** `List<RepoDto>`

**Örnek:**
```
POST /repos/query
Body: { "ownerType": "USER", "ownerLogin": "octocat" }
```

---

## BRANCH ENDPOINT'LERİ

### 1. Branch'leri GitHub'dan Çek ve Kaydet
**Method:** `POST`  
**URL:** `/branches/init`  
**Headers:** `Authorization` (Bearer token)

**Query Parameters:**
- `fullName` (zorunlu): Repository full name (örn: "owner/repo-name")

**Response:** `List<BranchDto>`

**Örnek:**
```
POST /branches/init?fullName=owner/repo-name
```

---

### 2. Branch'leri Veritabanından Getir
**Method:** `GET`  
**URL:** `/branches/query`  
**Headers:** `Authorization` (Bearer token)

**Query Parameters:**
- `fullName` (zorunlu): Repository full name (örn: "owner/repo-name")

**Response:** `List<BranchDto>`

**Örnek:**
```
GET /branches/query?fullName=owner/repo-name
```

---

## COMMIT ENDPOINT'LERİ

### 1. Commit'leri GitHub'dan Çek ve Kaydet
**Method:** `POST`  
**URL:** `/commits/init`  
**Headers:** `Authorization` (Bearer token)

**Request Body:**
```json
{
  "fullName": "owner/repo-name",
  "branchName": "main",
  "since": "2024-01-01T00:00:00Z"  // Opsiyonel, ISO 8601 formatı
}
```

**Response:** `List<CommitDto>`

---

### 2. Commit'leri Veritabanından Getir
**Method:** `POST`  
**URL:** `/commits/query`  
**Headers:** `Authorization` (Bearer token)

**Request Body:**
```json
{
  "fullName": "owner/repo-name",
  "branchName": "main"  // Opsiyonel
}
```

**Response:** `List<CommitDto>`

**Örnekler:**
```
POST /commits/query
Body: { "fullName": "owner/repo-name" }  // Tüm repo commit'leri

POST /commits/query
Body: { "fullName": "owner/repo-name", "branchName": "main" }  // Belirli branch commit'leri
```

---

## LANGUAGE ENDPOINT'LERİ

### 1. Language'leri GitHub'dan Çek ve Kaydet
**Method:** `POST`  
**URL:** `/languages/init`  
**Headers:** `Authorization` (Bearer token)

**Request Body:**
```json
{
  "repositoryId": 1
}
```

**Response:** `LanguageDto`

---

### 2. Language'leri Veritabanından Getir
**Method:** `GET`  
**URL:** `/languages/repository/{repositoryId}`  
**Headers:** `Authorization` (Bearer token)

**Path Variables:**
- `repositoryId` (zorunlu): Repository ID

**Response:** `LanguageDto`

---

### 3. Language'leri UI Formatında Getir
**Method:** `GET`  
**URL:** `/languages/repository/{repositoryId}/ui`  
**Headers:** `Authorization` (Bearer token)

**Path Variables:**
- `repositoryId` (zorunlu): Repository ID

**Response:** `LanguageUiDto`

---

## Özet Tablo

| Endpoint | Method | İşlem | Kaynak |
|----------|--------|-------|--------|
| `/repos/init/user` | POST | Repo'ları GitHub'dan çek ve kaydet | GitHub API |
| `/repos/query` | POST | Repo'ları getir | Veritabanı |
| `/branches/init` | POST | Branch'leri GitHub'dan çek ve kaydet | GitHub API |
| `/branches/query` | GET | Branch'leri getir | Veritabanı |
| `/commits/init` | POST | Commit'leri GitHub'dan çek ve kaydet | GitHub API |
| `/commits/query` | POST | Commit'leri getir | Veritabanı |
| `/languages/init` | POST | Language'leri GitHub'dan çek ve kaydet | GitHub API |
| `/languages/repository/{id}` | GET | Language'leri getir | Veritabanı |
| `/languages/repository/{id}/ui` | GET | Language'leri UI formatında getir | Veritabanı |

---

## Önemli Notlar

1. **Init Endpoint'leri:** GitHub API'den veri çeker ve veritabanına kaydeder
2. **Query Endpoint'leri:** Sadece veritabanından veri okur, GitHub API'ye istek atmaz
3. **fullName Formatı:** `owner/repo-name` formatında olmalıdır
4. **since Formatı:** ISO 8601 formatında tarih (örn: `2024-01-01T00:00:00Z`)
5. **Authorization:** Tüm endpoint'lerde JWT token zorunludur
6. **Request Body:** POST endpoint'lerinde `Content-Type: application/json` header'ı gereklidir

---

## Test Senaryosu

1. **Repo'ları init et:**
   ```
   POST /repos/init/user
   ```

2. **Branch'leri init et:**
   ```
   POST /branches/init?fullName=owner/repo-name
   ```

3. **Commit'leri init et:**
   ```
   POST /commits/init
   Body: { "fullName": "owner/repo-name", "branchName": "main", "since": "2024-01-01T00:00:00Z" }
   ```

4. **Language'leri init et:**
   ```
   POST /languages/init
   Body: { "repositoryId": 1 }
   ```

5. **Verileri oku:**
   ```
   POST /repos/query
   Body: { "ownerType": "USER" }

   GET /branches/query?fullName=owner/repo-name

   POST /commits/query
   Body: { "fullName": "owner/repo-name", "branchName": "main" }

   GET /languages/repository/1/ui
   ```
