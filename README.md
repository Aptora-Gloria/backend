# 🧠 Optimetric Mini GitHub Analytics Platform

Optimetric Analytics is a modular backend application built with Spring Boot. It provides comprehensive insights for GitHub repositories including contributor stats, commit tracking, repository languages, and content structure.

Each functional module is designed with its own:
- DTO (Data Transfer Object)
- Entity (Database Model)
- Mapper (Object Conversion)
- Repository (Persistence Layer)
- Service & ServiceImpl (Business Logic)

---

## 🚀 Modules Overview

### 1. 📊 Contributor Stats Module
Handles the collection and persistence of contributor statistics from GitHub.

- `POST /api/github/stats/init`
- `GET /api/github/stats`

### 2. 📆 Commit Module
Tracks commit metadata and content from repositories.

- `POST /api/github/commits/init`
- `GET /api/github/commits`

### 3. 📁 Content Module
Retrieves and stores the file/folder structure of a GitHub repository.

- `POST /api/github/content/init`
- `GET /api/github/content`

### 4. 🌐 Language Module
Fetches and stores programming language usage statistics.

- `POST /api/github/languages/init`
- `GET /api/github/languages`

### 5. 📘 Repository Metadata Module
Provides basic metadata about the GitHub repository.

- `GET /api/github/repo`

---

## 🧱 Project Architecture

```
src/
├── main/
│   ├── java/com.optimetric/
│   │   ├── auth/               # OAuth GitHub login and JWT logic
│   │   ├── github/             # GitHub-related modules
│   │   │   ├── controller/     # REST endpoints
│   │   │   ├── dto/            # Request/Response models
│   │   │   ├── entity/         # Database entities
│   │   │   ├── mapper/         # DTO-Entity conversion
│   │   │   ├── repository/     # Spring JPA repositories
│   │   │   ├── service/        # Service interfaces
│   │   │   └── service/impl/   # Business logic implementation
│   └── resources/
│       └── application.yml     # Spring Boot configuration
```

---

## ⚙️ Tech Stack

- Java 17
- Spring Boot 3
- Spring Security (JWT + OAuth2)
- PostgreSQL
- WebClient (for GitHub API)
- Maven
- Docker (optional)
- Lombok

---

## 🧪 Testing the API

All endpoints require a JWT token.

1. Authenticate with GitHub token:

```http
POST /api/auth/login
Authorization: Bearer {github_access_token}
```

2. Use the returned JWT for all other requests:

```http
Authorization: Bearer {jwt_token}
```

3. Test using Postman collection:
- `postman/optimetric-api.postman_collection.json`

---

## 📦 Setup & Installation

### Requirements

- Java 17+
- Maven 3.8+
- PostgreSQL
- GitHub OAuth App (Client ID + Secret)

### Run Locally
backend: -mvn clean install
         -mvn spring-boot:run

---

## Troubleshooting

### Lombok ve Annotation Processing Sorunları

Eğer IDE'nizde Lombok annotation'ları (örn: `@RequiredArgsConstructor`, `@Builder`) okunamıyorsa:

1. **Hızlı Çözüm:** `QUICK_FIX_LOMBOK.md` dosyasına bakın
2. **Detaylı Çözüm:** `TROUBLESHOOTING.md` dosyasına bakın

Yaygın sorunlar:
- Lombok plugin yüklü değil
- Annotation processing aktif değil
- IDE cache sorunları
- Maven projesi doğru import edilmemiş

---