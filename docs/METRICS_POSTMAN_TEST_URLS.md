# Metrics Service - Postman Test URL'leri

## Base URL
```
http://localhost:8080/api/metrics
```

---

## 1. POST - Get All User Metrics (Comprehensive)

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/user`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body (JSON):**
```json
{
  "login": "username"
}
```

**Örnek:**
```json
{
  "login": "arifferarslan"
}
```

**Response:** Tüm metrikleri içeren `UserMetricsDto` döner.

---

## 2. POST - Get PR Merge Rate Metrics

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/pr-merge-rate`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body (JSON):**
```json
{
  "login": "username"
}
```

**Response Örneği:**
```json
{
  "totalPullRequests": 234,
  "mergedPullRequests": 198,
  "openPullRequests": 12,
  "closedPullRequests": 24,
  "mergeRate": 84.62,
  "averageMergeTimeHours": 48.5
}
```

---

## 3. POST - Get PR Size Metrics

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/pr-size`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body (JSON):**
```json
{
  "login": "username"
}
```

**Response Örneği:**
```json
{
  "averageAdditions": 125.5,
  "averageDeletions": 89.2,
  "averageChangedFiles": 8.3,
  "averageTotalChanges": 214.7,
  "smallPrCount": 45,
  "mediumPrCount": 120,
  "largePrCount": 15,
  "smallPrPercentage": 25.0,
  "mediumPrPercentage": 66.67,
  "largePrPercentage": 8.33
}
```

---

## 4. POST - Get Review Statistics

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/review-stats`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body (JSON):**
```json
{
  "login": "username"
}
```

**Response Örneği:**
```json
{
  "totalReviews": 456,
  "approvedReviews": 320,
  "changesRequestedReviews": 45,
  "commentedReviews": 91,
  "averageReviewsPerPr": 2.5,
  "approvalRate": 70.18,
  "reviewsByState": {
    "APPROVED": 320,
    "CHANGES_REQUESTED": 45,
    "COMMENTED": 91
  },
  "prsWithoutReviews": 12,
  "prsWithReviewsPercentage": 94.87
}
```

---

## 5. POST - Get Commit Time Analysis

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/commit-time-analysis`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body (JSON):**
```json
{
  "login": "username"
}
```

**Response Örneği:**
```json
{
  "commitsByHour": {
    "9": 45,
    "10": 67,
    "14": 89,
    "15": 102,
    "16": 78
  },
  "commitsByDayOfWeek": {
    "MONDAY": 120,
    "TUESDAY": 145,
    "WEDNESDAY": 134,
    "THURSDAY": 156,
    "FRIDAY": 98,
    "SATURDAY": 23,
    "SUNDAY": 15
  },
  "weekdayCommits": 653,
  "weekendCommits": 38,
  "weekdayPercentage": 94.51,
  "weekendPercentage": 5.49,
  "mostActiveHour": 15,
  "mostActiveDay": "THURSDAY",
  "last30DaysCommits": 234,
  "last7DaysCommits": 45
}
```

---

## 6. POST - Get Language Diversity

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/language-diversity`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body (JSON):**
```json
{
  "login": "username"
}
```

**Response Örneği:**
```json
{
  "totalLanguages": 8,
  "uniqueLanguagesAcrossRepos": 8,
  "averageLanguagesPerRepo": 2.5,
  "languageUsageCount": {
    "Java": 12,
    "TypeScript": 8,
    "Python": 5,
    "JavaScript": 3
  },
  "languagePercentageDistribution": {
    "Java": 45.2,
    "TypeScript": 30.5,
    "Python": 15.8,
    "JavaScript": 8.5
  },
  "topLanguages": [
    {
      "language": "Java",
      "repoCount": 12,
      "totalPercentage": 45.2,
      "totalBytes": 1250000
    },
    {
      "language": "TypeScript",
      "repoCount": 8,
      "totalPercentage": 30.5,
      "totalBytes": 845000
    }
  ],
  "singleLanguageRepos": 5,
  "multiLanguageRepos": 10,
  "multiLanguageReposPercentage": 66.67
}
```

---

## 7. POST - Get Activity Trend

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/activity-trend`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body (JSON):**
```json
{
  "login": "username"
}
```

**Response Örneği:**
```json
{
  "weeklyTrend": [
    {
      "week": "2024-W01",
      "commits": 45,
      "pullRequests": 12,
      "reviews": 23,
      "totalActivity": 80
    },
    {
      "week": "2024-W02",
      "commits": 52,
      "pullRequests": 15,
      "reviews": 28,
      "totalActivity": 95
    }
  ],
  "monthlyTrend": [
    {
      "month": "2024-01",
      "commits": 234,
      "pullRequests": 56,
      "reviews": 89,
      "totalActivity": 379
    },
    {
      "month": "2024-02",
      "commits": 267,
      "pullRequests": 62,
      "reviews": 95,
      "totalActivity": 424
    }
  ],
  "totalActivityLast30Days": 456,
  "totalActivityLast7Days": 89,
  "activityGrowthRate": 12.5,
  "mostActiveWeek": "2024-W08",
  "mostActiveMonth": "2024-02",
  "last30DaysBreakdown": {
    "commits": 234,
    "pullRequests": 56,
    "reviews": 89,
    "commitsByDay": {
      "2024-02-01": 12,
      "2024-02-02": 15,
      "2024-02-03": 8
    },
    "prsByDay": {
      "2024-02-01": 3,
      "2024-02-02": 5,
      "2024-02-03": 2
    }
  }
}
```

---

## Postman Collection Örneği

### Collection Variables:
```
baseUrl: http://localhost:8080
jwtToken: <YOUR_JWT_TOKEN>
userLogin: arifferarslan
```

### Request Examples:

#### 1. Get All Metrics
```
POST {{baseUrl}}/api/metrics/user
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "login": "{{userLogin}}"
}
```

#### 2. Get PR Merge Rate
```
POST {{baseUrl}}/api/metrics/pr-merge-rate
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "login": "{{userLogin}}"
}
```

#### 3. Get PR Size
```
POST {{baseUrl}}/api/metrics/pr-size
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "login": "{{userLogin}}"
}
```

#### 4. Get Review Stats
```
POST {{baseUrl}}/api/metrics/review-stats
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "login": "{{userLogin}}"
}
```

#### 5. Get Commit Time Analysis
```
POST {{baseUrl}}/api/metrics/commit-time-analysis
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "login": "{{userLogin}}"
}
```

#### 6. Get Language Diversity
```
POST {{baseUrl}}/api/metrics/language-diversity
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "login": "{{userLogin}}"
}
```

#### 7. Get Activity Trend
```
POST {{baseUrl}}/api/metrics/activity-trend
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "login": "{{userLogin}}"
}
```

---

## Notlar

1. **Authentication:** Tüm endpoint'ler `@AuthenticationPrincipal` kullanıyor, JWT token gerekli.

2. **Request Body:** Tüm endpoint'ler POST method kullanıyor ve request body ile `login` parametresi alıyor.

3. **Validation:** `login` alanı `@NotBlank` validation'a sahip, boş olamaz.

4. **Response Format:** Tüm response'lar JSON formatında döner.

5. **Error Handling:** 
   - `UserNotFoundException`: Kullanıcı bulunamazsa 404 döner
   - `400 Bad Request`: Validation hatası varsa
   - `401 Unauthorized`: Token geçersizse

6. **Performance:** 
   - Metrikler `@Transactional(readOnly = true)` ile optimize edilmiştir
   - Büyük veri setleri için response süresi artabilir
   - İlk çağrıda hesaplama yapılır, sonraki çağrılarda cache kullanılabilir (ileride eklenebilir)

7. **Test Sırası:**
   - Önce `/user` endpoint'ini çağırarak tüm metrikleri görebilirsiniz
   - Sonra spesifik metrikler için diğer endpoint'leri kullanabilirsiniz

