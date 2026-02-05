refactor(repo-service): remove API DTO leakage from application layer

ISSUE 1 - Application Contract Split: Application katmanından API DTO
sızıntısını kaldırarak hexagonal architecture prensiplerini uyguladık.

Changes:
- Application DTO'ları oluşturuldu:
  * RepositoryInfo: Application layer için repository bilgisi contract'ı
  * RepositoryInitData: Application layer için repository initialization contract'ı

- Mapper'lar oluşturuldu:
  * RepositoryMapper: Application DTO ↔ Entity mapping
  * ApplicationToApiMapper: Application DTO ↔ API DTO mapping (Controller'da)
  * GitHubRepositoryMapper: GitHub API Response → Application DTO mapping

- Port'lar güncellendi:
  * RepoServicePort: RepositoryInfo ve RepositoryInitData kullanıyor
  * RepoClientPort: RepositoryInitData kullanıyor

- Adapter'lar güncellendi:
  * RepoServiceAdapter: Application DTO'ları kullanıyor
  * RepoClientAdapter: GitHub API response'u RepositoryInitData'ya map ediyor

- Controller güncellendi:
  * RepoController: Application DTO'ları API DTO'larına map ediyor

Verification:
- git grep "com.optimetric.github.api" src/main/java/com/optimetric/github/application/port/RepoServicePort.java → 0 sonuç ✅
- git grep "com.optimetric.github.api" src/main/java/com/optimetric/github/application/client/RepoClientPort.java → 0 sonuç ✅

Acceptance Criteria:
✅ Application katmanı artık API DTO'larını bilmiyor
✅ Tüm mapping'ler controller katmanında yapılıyor
✅ Hexagonal architecture prensipleri korunuyor

