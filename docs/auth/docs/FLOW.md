# Auth servis akışı

## Login akışı

1. İstemci `POST /auth/login` ile GitHub OAuth `code` gönderir.
2. **AuthController** isteği alır, `LoginPort` (LoginUseCase) çağırır.
3. **LoginUseCase** `AuthenticationDomainService.login(code)` çağırır.
4. **AuthenticationDomainService:**
   - GitHub OAuth port ile kodu access token’a çevirir (GitHubOAuthAdapter → GitHub API).
   - GitHub’dan kullanıcı bilgisi alır.
   - Kullanıcı yoksa oluşturur, varsa günceller (UserPersistencePort).
   - JwtTokenPort ile JWT üretir.
5. JWT `AuthResponse.accessToken` olarak döner.

## Mevcut kullanıcı (me) akışı

1. İstemci `GET /auth/me` ile `Authorization: Bearer <jwt>` gönderir.
2. **AuthController** token’ı alır, `GetCurrentUserPort` (GetCurrentUserUseCase) çağırır.
3. **GetCurrentUserUseCase** JWT’den login çıkarır, `UserPersistencePort.findByLogin` ile kullanıcıyı bulur.
4. Kullanıcı bilgisi API DTO’ya map edilip döner; bulunamazsa 404/exception.

## Bağımlılıklar

- **Giriş:** GitHub OAuth (code → token, token → user). GitHub modülü `GitHubServicePort` ile token ve user sağlar.
- **Çıkış:** JWT (JwtTokenPort), kalıcılık (UserPersistencePort).
