# Postman Test İstekleri - Branch, Commit ve Language

## Base URL
```
http://localhost:8080
```

## Authorization Header
Tüm isteklerde `Authorization` header'ı gereklidir (init endpoint'leri için):
```
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## 🌿 BRANCH ENDPOINT'LERİ

### 1. Branch'leri GitHub'dan Çek ve Kaydet (Init)
**Method:** `POST`  
**URL:** `http://localhost:8080/api/github/branches/init?repo_id=1`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Query Parameters:**
- `repo_id`: `1` (Repository ID - Long)

**Örnek Request:**
```
POST http://localhost:8080/api/github/branches/init?repo_id=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Örnek Response:**
```json
[
  {
    "name": "main",
    "repo_id": 1
  },
  {
    "name": "develop",
    "repo_id": 1
  },
  {
    "name": "feature/new-feature",
    "repo_id": 1
  }
]
```

---

### 2. Branch'leri Veritabanından Getir
**Method:** `GET`  
**URL:** `http://localhost:8080/api/github/branches?repo_id=1`

**Query Parameters:**
- `repo_id`: `1` (Repository ID - Long)

**Örnek Request:**
```
GET http://localhost:8080/api/github/branches?repo_id=1
```

**Örnek Response:**
```json
[
  {
    "name": "main",
    "repo_id": 1
  },
  {
    "name": "develop",
    "repo_id": 1
  }
]
```

---

## 📝 COMMIT ENDPOINT'LERİ

### 1. Commit'leri GitHub'dan Çek ve Kaydet (Init)
**Method:** `POST`  
**URL:** `http://localhost:8080/api/github/commits/init`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "repositoryId": 1,
  "branchId": 1
}
```

**Örnek Request:**
```
POST http://localhost:8080/api/github/commits/init
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "repositoryId": 1,
  "branchId": 1
}
```

**Örnek Response:**
```json
[
  {
    "sha": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0",
    "nodeId": "MDY6Q29tbWl0MTIzNDU2Nzg5",
    "commit": {
      "message": "Initial commit\n\nThis is the first commit",
      "author": {
        "name": "John Doe",
        "email": "john.doe@example.com",
        "date": "2024-01-15T10:30:00Z"
      },
      "committer": {
        "name": "John Doe",
        "email": "john.doe@example.com",
        "date": "2024-01-15T10:30:00Z"
      }
    },
    "author": {
      "login": "johndoe"
    },
    "committer": {
      "login": "johndoe"
    },
    "parents": [
      {
        "sha": "b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0a1"
      }
    ],
    "htmlUrl": "https://github.com/johndoe/my-repo/commit/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0",
    "apiUrl": "https://api.github.com/repos/johndoe/my-repo/commits/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0"
  }
]
```

---

### 2. Repository'ye Göre Commit'leri Getir
**Method:** `GET`  
**URL:** `http://localhost:8080/api/github/commits/repository/1`

**Path Variables:**
- `repositoryId`: `1` (Repository ID - Long)

**Örnek Request:**
```
GET http://localhost:8080/api/github/commits/repository/1
```

**Örnek Response:**
```json
[
  {
    "sha": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0",
    "nodeId": "MDY6Q29tbWl0MTIzNDU2Nzg5",
    "commit": {
      "message": "Initial commit",
      "author": {
        "name": "John Doe",
        "email": "john.doe@example.com",
        "date": "2024-01-15T10:30:00Z"
      },
      "committer": {
        "name": "John Doe",
        "email": "john.doe@example.com",
        "date": "2024-01-15T10:30:00Z"
      }
    },
    "author": {
      "login": "johndoe"
    },
    "committer": {
      "login": "johndoe"
    },
    "parents": null,
    "htmlUrl": "https://github.com/johndoe/my-repo/commit/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0",
    "apiUrl": "https://api.github.com/repos/johndoe/my-repo/commits/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0"
  }
]
```

---

### 3. Branch'e Göre Commit'leri Getir
**Method:** `GET`  
**URL:** `http://localhost:8080/api/github/commits/branch/1`

**Path Variables:**
- `branchId`: `1` (Branch ID - Long)

**Örnek Request:**
```
GET http://localhost:8080/api/github/commits/branch/1
```

**Örnek Response:**
```json
[
  {
    "sha": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0",
    "nodeId": "MDY6Q29tbWl0MTIzNDU2Nzg5",
    "commit": {
      "message": "Feature: Add new functionality",
      "author": {
        "name": "Jane Smith",
        "email": "jane.smith@example.com",
        "date": "2024-01-20T14:45:00Z"
      },
      "committer": {
        "name": "Jane Smith",
        "email": "jane.smith@example.com",
        "date": "2024-01-20T14:45:00Z"
      }
    },
    "author": {
      "login": "janesmith"
    },
    "committer": {
      "login": "janesmith"
    },
    "parents": null,
    "htmlUrl": "https://github.com/johndoe/my-repo/commit/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0",
    "apiUrl": "https://api.github.com/repos/johndoe/my-repo/commits/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0"
  }
]
```

---

### 4. Repository ve Branch'e Göre Commit'leri Getir
**Method:** `GET`  
**URL:** `http://localhost:8080/api/github/commits/repository/1/branch/1`

**Path Variables:**
- `repositoryId`: `1` (Repository ID - Long)
- `branchId`: `1` (Branch ID - Long)

**Örnek Request:**
```
GET http://localhost:8080/api/github/commits/repository/1/branch/1
```

**Örnek Response:**
```json
[
  {
    "sha": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0",
    "nodeId": "MDY6Q29tbWl0MTIzNDU2Nzg5",
    "commit": {
      "message": "Initial commit",
      "author": {
        "name": "John Doe",
        "email": "john.doe@example.com",
        "date": "2024-01-15T10:30:00Z"
      },
      "committer": {
        "name": "John Doe",
        "email": "john.doe@example.com",
        "date": "2024-01-15T10:30:00Z"
      }
    },
    "author": {
      "login": "johndoe"
    },
    "committer": {
      "login": "johndoe"
    },
    "parents": null,
    "htmlUrl": "https://github.com/johndoe/my-repo/commit/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0",
    "apiUrl": "https://api.github.com/repos/johndoe/my-repo/commits/a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0"
  }
]
```

---

## 📋 Postman Collection Özeti

### Branch Endpoints
1. ✅ `POST /api/github/branches/init?repo_id={id}` - Branch'leri init et
2. ✅ `GET /api/github/branches?repo_id={id}` - Branch'leri getir

### Commit Endpoints
1. ✅ `POST /api/github/commits/init` - Commit'leri init et
2. ✅ `GET /api/github/commits/repository/{repositoryId}` - Repository'ye göre getir
3. ✅ `GET /api/github/commits/branch/{branchId}` - Branch'e göre getir
4. ✅ `GET /api/github/commits/repository/{repositoryId}/branch/{branchId}` - Her ikisine göre getir

---

## 🔑 Önemli Notlar

1. **JWT Token:** `Authorization` header'ında `Bearer {token}` formatında gönderilmelidir.
2. **Repository ID ve Branch ID:** Veritabanında mevcut olmalıdır. Önce repository ve branch oluşturulmalıdır.
3. **Init İşlemleri:** GitHub API'den veri çeker ve veritabanına kaydeder.
4. **GET İşlemleri:** Sadece veritabanından veri okur, GitHub API'ye istek atmaz.
5. **Unique Constraint:** Aynı branch'te aynı SHA'ya sahip commit olamaz, farklı branch'lerde olabilir.

---

## 🧪 Test Senaryosu Örneği

1. **Repository oluştur** (başka bir endpoint ile)
2. **Branch'leri init et:**
   ```
   POST /api/github/branches/init?repo_id=1
   ```
3. **Branch'leri kontrol et:**
   ```
   GET /api/github/branches?repo_id=1
   ```
4. **Commit'leri init et (örneğin main branch için):**
   ```
   POST /api/github/commits/init
   Body: { "repositoryId": 1, "branchId": 1 }
   ```
5. **Commit'leri kontrol et:**
   ```
   GET /api/github/commits/repository/1/branch/1
   ```

