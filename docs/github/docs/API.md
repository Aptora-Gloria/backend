# GitHub API

## Endpoint özeti

Tüm endpoint'ler JWT ile yetkilendirilir (`@AuthenticationPrincipal String login`).

### Repos

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/api/github/repos/init/user` | Kullanıcının repolarını init eder |
| POST | `/api/github/repos/query` | Repo listesi (body: owner type, owner login) |

### Branches

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/api/github/branches/init` | Branch’leri init eder |
| GET | `/api/github/branches/query` | Branch listesi |

### Commits

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/api/github/commits/init` | Commit’leri init eder |
| POST | `/api/github/commits/query` | Commit listesi |

### Pull requests

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/api/github/pull-requests/query` | Repo için PR listesi |
| POST | `/api/github/pull-requests/query/single` | Tekil PR |
| POST | `/api/github/pull-requests/reviews` | PR review’ları |
| POST | `/api/github/pull-requests/commits` | PR commit’leri |
| POST | `/api/github/pull-requests/sync/initiate` | PR sync başlatır |
| POST | `/api/github/pull-requests/sync/status` | Sync durumu |

### Languages

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/api/github/languages/init` | Dilleri init eder |
| POST | `/api/github/languages/query` | Dil listesi (DB) |
| POST | `/api/github/languages/query/ui` | UI için dil listesi |

### Repository sync status

| Method | Path | Açıklama |
|--------|------|----------|
| POST | `/api/github/repositories/sync/status` | Repo sync/hazırlık durumu |

## Contract notu

Request/response tipleri ilgili controller ve `api.request` / `api.response` paketlerinde tanımlıdır. Ortak alanlar: `repoFullName`, `ownerLogin`, `ownerType`, PR için `prNumber`, sync için `repoFullName` vb.
