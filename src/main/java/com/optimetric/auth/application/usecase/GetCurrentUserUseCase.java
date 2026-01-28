package com.optimetric.auth.application.usecase;

import com.optimetric.auth.application.dto.UserInfo;
import com.optimetric.auth.domain.service.AuthenticationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case for getting current user information.
 * This is an inbound adapter that retrieves user information from JWT token.
 */
@Service
@RequiredArgsConstructor
public class GetCurrentUserUseCase {

    private final AuthenticationDomainService authenticationDomainService;

    public UserInfo execute(String token) {
        return authenticationDomainService.getCurrentUser(token);
    }
}
