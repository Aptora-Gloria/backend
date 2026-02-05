package com.optimetric.auth.application;

import com.optimetric.auth.application.dto.LoginCommand;
import com.optimetric.auth.application.dto.LoginResult;
import com.optimetric.auth.application.dto.UserInfo;

import java.util.Optional;

// Kimlik doğrulama işlemleri için uygulama portu; Application DTO kullanır.
public interface AuthServicePort {

    // GitHub OAuth kodu ile giriş; JWT döner.
    LoginResult login(LoginCommand command);

    // JWT ile mevcut kullanıcı bilgisini döner.
    UserInfo getCurrentUser(String token);

    // Login ile kullanıcı arar.
    Optional<UserInfo> findByLogin(String login);

    // JWT'den login çıkarır.
    String getLoginFromToken(String token);

    // JWT'den GitHub access token döner.
    String getAccessTokenFromToken(String jwtToken);

    // Login ile kullanıcının GitHub access token'ını döner.
    String getAccessTokenFromLogin(String login);

    // Login ile kullanıcı arar; yoksa UserNotFoundException fırlatır.
    UserInfo findByLoginOrThrow(String login);

    // Dahili id ile kullanıcı arar; diğer modüller için.
    Optional<UserInfo> findById(Long id);
}
