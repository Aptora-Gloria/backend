# GitHub veritabanı

## Tablolar

| Tablo | Açıklama |
|-------|----------|
| repositories | Repo meta verisi (full_name, github_repo_id, user ilişkisi) |
| github_branches | Repo branch’leri |
| github_repository_commits | Repo commit’leri |
| github_repository_languages | Repo dil istatistikleri |
| github_pull_requests | PR üst bilgisi ve data level |
| github_pull_request_commits | PR–commit ilişkisi (github_repo_id + sha UNIQUE) |
| github_pull_request_reviews | PR review’ları |
| github_sync_jobs | Sync job kayıtları (tip, durum, cursor, hata) |

## Kısıtlar

- **repositories:** `github_repo_id` UNIQUE; kullanıcı–repo ilişkisi.
- **github_pull_request_commits:** `(github_repo_id, sha)` UNIQUE.
- **github_sync_jobs:** `job_type`, `repo_full_name`, `status`, `cursor`; sayfalama ve hata bilgisi.

## İlişkiler

- Branch, commit, language, pull request ve sync job kayıtları repo (veya repo full name) ile ilişkilidir.
- PR commit ve review kayıtları ilgili PR ve repo ile ilişkilidir.
