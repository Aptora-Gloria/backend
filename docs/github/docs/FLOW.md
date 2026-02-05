# GitHub servis akışı

## Repo init akışı

1. İstemci `POST /api/github/repos/init/user` ile kullanıcı repolarını init eder.
2. **InitializeUserRepositoriesPort** (use case) kullanıcı için GitHub’dan repo listesi alır, DB’ye yazar/günceller.
3. HTTP adapter (GitHub API) ve persistence adapter (RepositoryPersistenceAdapter) kullanılır.

## Branch / commit / language init

1. İlgili controller (Branch, Commit, Language) init endpoint’ini çağırır.
2. Port’lar (InitBranchesPort, vb.) GitHub client ve persistence ile veriyi çekip DB’ye yazar.

## Pull request sorgulama

1. **GetPullRequestsPort** / **GetPullRequestPort** repo ve isteğe göre PR listesi veya tekil PR döner.
2. Veri önce DB’den (PullRequestQueryPort); gerekirse GitHub API ile zenginleştirilir.
3. Data level ve sync durumu policy (PullRequestSyncPolicy, DataLevelPolicy) ile belirlenir.

## PR sync akışı

1. İstemci `POST .../sync/initiate` ile PR sync başlatır.
2. **InitiatePullRequestSyncPort** sync job oluşturur veya mevcut job’ı kullanır.
3. **SyncJobServiceAdapter** ve client adapter’lar sayfalı GitHub API çağrıları yapar; PR, commit, review verisi yazılır.
4. `POST .../sync/status` ile job durumu ve ilerleme sorgulanır (GetSyncStatusPort).

## Bağımlılıklar

- **Giriş:** Auth modülünden kullanıcı/login; GitHub API (token ile).
- **Çıkış:** JPA repository’ler; shared exception ve domain (OwnerType vb.).
