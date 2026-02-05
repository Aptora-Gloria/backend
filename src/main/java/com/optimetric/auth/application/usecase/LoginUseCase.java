package com.optimetric.auth.application.usecase;

import com.optimetric.auth.application.dto.LoginCommand;
import com.optimetric.auth.application.dto.LoginResult;
import com.optimetric.auth.application.port.in.LoginPort;
import com.optimetric.auth.domain.service.AuthenticationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Kullanıcı giriş use case; LoginPort (inbound) uygular.
@Service
@RequiredArgsConstructor
public class LoginUseCase implements LoginPort {

    private final AuthenticationDomainService authenticationDomainService;

    @Override
    public LoginResult execute(LoginCommand command) {
        String jwtToken = authenticationDomainService.login(command.code());
        return new LoginResult(jwtToken);
    }
}
