# Repository Metrics Service - Postman Test URL'leri

## Base URL
```
http://localhost:8080/api/metrics/repository
```

---

## 1. POST - Get All Repository Metrics (Comprehensive)

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/repository`

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

**Response:** Tüm metrikleri içeren `RepositoryMetricsDto` döner.

---

## 2. POST - Get Repository Overview

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/repository/overview`

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

**Response Örneği:**
```json
{
  "fullName": "facebook/react",
  "name": "react",
  "description": "The library for web and native user interfaces",
  "htmlUrl": "https://github.com/facebook/react",
  "ownerLogin": "facebook",
  "ownerType": "ORGANIZATION",
  "defaultBranch": "main",
  "primaryLanguage": "JavaScript",
  "stars": 225000,
  "forks": 45000,
  "watchers": 12000,
  "openIssues": 567,
  "isPrivate": false,
  "isFork": false,
  "size": 102400,
  "createdAt": "2013-05-24T16:15:30Z",
  "updatedAt": "2024-02-15T14:30:00Z",
  "pushedAt": "2024-02-15T14:30:00Z",
  "lastSyncedAt": "2024-02-15T10:00:00Z",
  "repositoryAgeDays": 3920,
  "isActive": true,
  "daysSinceLastPush": 0
}
```

---

## 3. POST - Get Pull Request Metrics

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/repository/pull-requests`

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

**Response Örneği:**
```json
{
  "totalPullRequests": 234,
  "openPullRequests": 12,
  "closedPullRequests": 24,
  "mergedPullRequests": 198,
  "draftPullRequests": 5,
  "mergeRate": 84.62,
  "averageAdditions": 125.5,
  "averageDeletions": 89.2,
  "averageChangedFiles": 8.3,
  "averageTotalChanges": 214.7,
  "largestPrSize": 2500,
  "smallestPrSize": 15,
  "smallPrCount": 45,
  "mediumPrCount": 120,
  "largePrCount": 15,
  "smallPrPercentage": 25.0,
  "mediumPrPercentage": 66.67,
  "largePrPercentage": 8.33,
  "prsLast30Days": 45,
  "prsLast7Days": 12,
  "mergedLast30Days": 38,
  "averageMergeTimeHours": 48.5,
  "uniqueAuthors": 25,
  "topAuthors": [
    {
      "authorLogin": "john",
      "prCount": 45,
      "mergedPrCount": 38,
      "mergeRate": 84.44
    }
  ],
  "prsByAuthor": {
    "john": 45,
    "jane": 32,
    "bob": 28
  }
}
```

---

## 4. POST - Get Commit Metrics

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/repository/commits`

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

**Response Örneği:**
```json
{
  "totalCommits": 1523,
  "mergeCommits": 234,
  "regularCommits": 1289,
  "mergeCommitPercentage": 15.36,
  "commitsLast30Days": 45,
  "commitsLast7Days": 12,
  "lastCommitDate": "2024-02-15T14:30:00Z",
  "mostActiveDay": "THURSDAY",
  "mostActiveHour": 15,
  "commitsByHour": {
    "9": 45,
    "10": 67,
    "14": 89,
    "15": 102
  },
  "commitsByDayOfWeek": {
    "MONDAY": 120,
    "TUESDAY": 145,
    "WEDNESDAY": 134,
    "THURSDAY": 156,
    "FRIDAY": 98
  },
  "weekdayCommits": 653,
  "weekendCommits": 38,
  "weekdayPercentage": 94.51,
  "weekendPercentage": 5.49,
  "uniqueAuthors": 15,
  "topAuthors": [
    {
      "authorLogin": "john",
      "commitCount": 234,
      "mergeCommitCount": 35
    }
  ],
  "commitsByAuthor": {
    "john": 234,
    "jane": 189,
    "bob": 156
  },
  "averageMessageLength": 45.2,
  "longestMessageLength": 250,
  "shortestMessageLength": 5
}
```

---

## 5. POST - Get Review Metrics

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/repository/reviews`

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

**Response Örneği:**
```json
{
  "totalReviews": 456,
  "approvedReviews": 320,
  "changesRequestedReviews": 45,
  "commentedReviews": 91,
  "reviewsByState": {
    "APPROVED": 320,
    "CHANGES_REQUESTED": 45,
    "COMMENTED": 91
  },
  "reviewsLast30Days": 89,
  "reviewsLast7Days": 23,
  "averageReviewsPerPr": 2.5,
  "prsWithoutReviews": 12,
  "prsWithReviewsPercentage": 94.87,
  "approvalRate": 70.18,
  "uniqueReviewers": 18,
  "topReviewers": [
    {
      "reviewerLogin": "alice",
      "reviewCount": 89,
      "approvalCount": 65,
      "approvalRate": 73.03
    }
  ],
  "reviewsByReviewer": {
    "alice": 89,
    "bob": 67,
    "charlie": 54
  },
  "approvalsByReviewer": {
    "alice": 65,
    "bob": 48,
    "charlie": 42
  }
}
```

---

## 6. POST - Get Branch Metrics

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/repository/branches`

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

**Response Örneği:**
```json
{
  "totalBranches": 15,
  "defaultBranch": "main",
  "topBranchesByCommits": [
    {
      "branchName": "main",
      "commitCount": 1523,
      "lastCommitDate": "2024-02-15T14:30:00Z",
      "isDefault": true
    },
    {
      "branchName": "develop",
      "commitCount": 234,
      "lastCommitDate": "2024-02-10T10:00:00Z",
      "isDefault": false
    }
  ],
  "commitsByBranch": {
    "main": 1523,
    "develop": 234,
    "feature/new-feature": 45
  },
  "branchesWithNoCommits": 2,
  "averageCommitsPerBranch": 120.2
}
```

---

## 7. POST - Get Language Metrics

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/repository/languages`

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

**Response Örneği:**
```json
{
  "totalLanguages": 3,
  "primaryLanguage": "Java",
  "primaryLanguagePercentage": 75.2,
  "languageDistribution": {
    "Java": 75.2,
    "TypeScript": 20.5,
    "JavaScript": 4.3
  },
  "languageBytes": {
    "Java": 1250000,
    "TypeScript": 340000,
    "JavaScript": 72000
  },
  "topLanguages": [
    {
      "language": "Java",
      "bytes": 1250000,
      "percentage": 75.2
    },
    {
      "language": "TypeScript",
      "bytes": 340000,
      "percentage": 20.5
    }
  ],
  "isMultiLanguage": true,
  "languageDiversityScore": 30.0
}
```

---

## 8. POST - Get Activity Trend

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/repository/activity`

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
  "last30DaysBreakdown": {
    "commits": 234,
    "pullRequests": 56,
    "reviews": 89
  },
  "activityGrowthRate": 12.5,
  "mostActiveWeek": "2024-W08",
  "mostActiveMonth": "2024-02",
  "commitsByDay": {
    "2024-02-01": 12,
    "2024-02-02": 15,
    "2024-02-03": 8
  },
  "prsByDay": {
    "2024-02-01": 3,
    "2024-02-02": 5,
    "2024-02-03": 2
  },
  "reviewsByDay": {
    "2024-02-01": 8,
    "2024-02-02": 12,
    "2024-02-03": 5
  }
}
```

---

## 9. POST - Get Code Quality Metrics

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/repository/code-quality`

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

**Response Örneği:**
```json
{
  "averagePrSize": 214.5,
  "prMergeRate": 84.62,
  "averageReviewsPerPr": 2.5,
  "prsWithoutReviewsPercentage": 5.13,
  "smallPrPercentage": 25.0,
  "mergeCommitPercentage": 15.36,
  "averageCommitMessageLength": 45.2,
  "regularCommitPercentage": 84.64,
  "netCodeChange": 12500,
  "codeGrowthRate": 12.5,
  "averageFilesChangedPerPr": 8.3,
  "overallQualityScore": 78.5
}
```

---

## 10. POST - Get Contributor Metrics

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/repository/contributors`

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

**Response Örneği:**
```json
{
  "totalContributors": 25,
  "activeContributorsLast30Days": 12,
  "activeContributorsLast7Days": 5,
  "topContributorsByCommits": [
    {
      "contributorLogin": "john",
      "commitCount": 234,
      "prCount": 45,
      "reviewCount": 23,
      "totalContributions": 302
    }
  ],
  "topContributorsByPrs": [
    {
      "contributorLogin": "jane",
      "commitCount": 189,
      "prCount": 52,
      "reviewCount": 18,
      "totalContributions": 259
    }
  ],
  "topContributorsByReviews": [
    {
      "contributorLogin": "alice",
      "commitCount": 45,
      "prCount": 12,
      "reviewCount": 89,
      "totalContributions": 146
    }
  ],
  "commitsByContributor": {
    "john": 234,
    "jane": 189,
    "bob": 156
  },
  "prsByContributor": {
    "jane": 52,
    "john": 45,
    "bob": 32
  },
  "reviewsByContributor": {
    "alice": 89,
    "bob": 67,
    "charlie": 54
  },
  "top10ContributorsPercentage": 75.5,
  "singleCommitContributors": 3,
  "regularContributors": 15
}
```

---

## 11. POST - Get Repository Health Score

**Method:** `POST`  
**URL:** `http://localhost:8080/api/metrics/repository/health`

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

**Response Örneği:**
```json
{
  "activityScore": 85.0,
  "popularityScore": 95.0,
  "codeQualityScore": 78.5,
  "communityScore": 80.0,
  "overallHealthScore": 84.25,
  "healthStatus": "EXCELLENT",
  "breakdown": {
    "activityWeight": 0.3,
    "popularityWeight": 0.2,
    "qualityWeight": 0.3,
    "communityWeight": 0.2
  }
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

#### 1. Get All Metrics
```
POST {{baseUrl}}/api/metrics/repository
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 2. Get Overview
```
POST {{baseUrl}}/api/metrics/repository/overview
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 3. Get Pull Request Metrics
```
POST {{baseUrl}}/api/metrics/repository/pull-requests
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 4. Get Commit Metrics
```
POST {{baseUrl}}/api/metrics/repository/commits
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 5. Get Review Metrics
```
POST {{baseUrl}}/api/metrics/repository/reviews
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 6. Get Branch Metrics
```
POST {{baseUrl}}/api/metrics/repository/branches
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 7. Get Language Metrics
```
POST {{baseUrl}}/api/metrics/repository/languages
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 8. Get Activity Trend
```
POST {{baseUrl}}/api/metrics/repository/activity
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 9. Get Code Quality
```
POST {{baseUrl}}/api/metrics/repository/code-quality
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 10. Get Contributor Metrics
```
POST {{baseUrl}}/api/metrics/repository/contributors
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

#### 11. Get Health Score
```
POST {{baseUrl}}/api/metrics/repository/health
Authorization: Bearer {{jwtToken}}
Content-Type: application/json

{
  "repoFullName": "{{repoFullName}}"
}
```

---

## Notlar

1. **Authentication:** Tüm endpoint'ler `@AuthenticationPrincipal` kullanıyor, JWT token gerekli.

2. **Request Body:** Tüm endpoint'ler POST method kullanıyor ve request body ile `repoFullName` parametresi alıyor.

3. **Validation:** `repoFullName` alanı `@NotBlank` validation'a sahip, boş olamaz. Format: `owner/repo-name`

4. **Response Format:** Tüm response'lar JSON formatında döner.

5. **Error Handling:** 
   - `RepositoryNotFoundException`: Repo bulunamazsa 404 döner
   - `UserNotFoundException`: Kullanıcı bulunamazsa 404 döner
   - `400 Bad Request`: Validation hatası varsa
   - `401 Unauthorized`: Token geçersizse

6. **Performance:** 
   - Metrikler `@Transactional(readOnly = true)` ile optimize edilmiştir
   - Tek repo için hesaplama yapıldığından user metriklerinden daha hızlıdır
   - İlk çağrıda hesaplama yapılır, sonraki çağrılarda cache kullanılabilir (ileride eklenebilir)

7. **Test Sırası:**
   - Önce `/overview` endpoint'ini çağırarak temel bilgileri görebilirsiniz
   - Sonra `/repository` endpoint'ini çağırarak tüm metrikleri görebilirsiniz
   - Spesifik metrikler için diğer endpoint'leri kullanabilirsiniz

8. **Health Status Değerleri:**
   - `EXCELLENT`: 80-100
   - `GOOD`: 60-79
   - `FAIR`: 40-59
   - `POOR`: 20-39
   - `CRITICAL`: 0-19

