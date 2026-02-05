package com.optimetric.auth.application.port.in;

import com.optimetric.auth.application.dto.LoginCommand;
import com.optimetric.auth.application.dto.LoginResult;

// Giriş inbound portu: GitHub OAuth kodu → JWT; controller çağırır, use case uygular.
public interface LoginPort {

    // OAuth kodu ile token alır, kullanıcıyı kaydeder, JWT döner.
    LoginResult execute(LoginCommand command);
}
