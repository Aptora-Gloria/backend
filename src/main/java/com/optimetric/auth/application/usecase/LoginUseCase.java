package com.optimetric.auth.application.usecase;

import com.optimetric.auth.application.dto.LoginCommand;
import com.optimetric.auth.application.dto.LoginResult;
import com.optimetric.auth.domain.service.AuthenticationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case for user login.
 * This is an inbound adapter that orchestrates the login flow.
 */
@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final AuthenticationDomainService authenticationDomainService;

    public LoginResult execute(LoginCommand command) {
        String jwtToken = authenticationDomainService.login(command.code());
        return new LoginResult(jwtToken);
    }
}
