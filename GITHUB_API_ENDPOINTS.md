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

## 📦 REPO ENDPOINT'LERİ

### 1. Kullanıcı Repo'larını GitHub'dan Çek ve Kaydet
**Method:** `POST`  
**URL:** `/repos/init/user`  
**Headers:** `Authorization` (Bearer token)

**Request:** Body yok

**Response:** `List<RepoInitrequest>`

---

### 2. Kullanıcı Repo'larını Getir
**Method:** `GET`  
**URL:** `/repos`  
**Headers:** `Authorization` (Bearer token)

**Query Parameters:**
- `ownerType` (opsiyonel): `USER` veya `ORGANIZATION`
- `ownerLogin` (opsiyonel): Owner login adı

**Response:** `List<RepoDto>`

**Örnek:**
```
GET /repos?ownerType=USER&ownerLogin=octocat
```

---

## 🌿 BRANCH ENDPOINT'LERİ

### 1. Branch'leri GitHub'dan Çek ve Kaydet
**Method:** `POST`  
**URL:** `/branches/init`  
**Headers:** `Authorization` (Bearer token)

**Request Body:**
```json
{
  "fullName": "owner/repo-name"
}
```

**Response:** `List<BranchDto>`

---

### 2. Branch'leri Veritabanından Getir
**Method:** `GET`  
**URL:** `/branches`  
**Headers:** `Authorization` (Bearer token)

**Query Parameters:**
- `fullName` (zorunlu): Repository full name (örn: "owner/repo-name")

**Response:** `List<BranchDto>`

**Örnek:**
```
GET /branches?fullName=owner/repo-name
```

---

## 📝 COMMIT ENDPOINT'LERİ

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
**Method:** `GET`  
**URL:** `/commits`  
**Headers:** `Authorization` (Bearer token)

**Query Parameters:**
- `fullName` (zorunlu): Repository full name (örn: "owner/repo-name")
- `branchName` (opsiyonel): Branch adı

**Response:** `List<CommitDto>`

**Örnekler:**
```
GET /commits?fullName=owner/repo-name                    // Tüm repo commit'leri
GET /commits?fullName=owner/repo-name&branchName=main    // Belirli branch commit'leri
```

---

## 📋 Özet Tablo

| Endpoint | Method | İşlem | Kaynak |
|----------|--------|-------|--------|
| `/repos/init/user` | POST | Repo'ları GitHub'dan çek ve kaydet | GitHub API |
| `/repos` | GET | Repo'ları getir | Veritabanı |
| `/branches/init` | POST | Branch'leri GitHub'dan çek ve kaydet | GitHub API |
| `/branches` | GET | Branch'leri getir | Veritabanı |
| `/commits/init` | POST | Commit'leri GitHub'dan çek ve kaydet | GitHub API |
| `/commits` | GET | Commit'leri getir | Veritabanı |

---

## 🔑 Önemli Notlar

1. **Init Endpoint'leri:** GitHub API'den veri çeker ve veritabanına kaydeder
2. **GET Endpoint'leri:** Sadece veritabanından veri okur, GitHub API'ye istek atmaz
3. **fullName Formatı:** `owner/repo-name` formatında olmalıdır
4. **since Formatı:** ISO 8601 formatında tarih (örn: `2024-01-01T00:00:00Z`)
5. **Authorization:** Tüm endpoint'lerde JWT token zorunludur

---

## 🧪 Test Senaryosu

1. **Repo'ları init et:**
   ```
   POST /repos/init/user
   ```

2. **Branch'leri init et:**
   ```
   POST /branches/init
   Body: { "fullName": "owner/repo-name" }
   ```

3. **Commit'leri init et:**
   ```
   POST /commits/init
   Body: { "fullName": "owner/repo-name", "branchName": "main", "since": "2024-01-01T00:00:00Z" }
   ```

4. **Verileri oku:**
   ```
   GET /repos
   GET /branches?fullName=owner/repo-name
   GET /commits?fullName=owner/repo-name&branchName=main
   ```

