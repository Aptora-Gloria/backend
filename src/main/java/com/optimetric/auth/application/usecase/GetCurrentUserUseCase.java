package com.optimetric.auth.application.usecase;

import com.optimetric.auth.application.dto.UserInfo;
import com.optimetric.auth.application.port.in.GetCurrentUserPort;
import com.optimetric.auth.domain.service.AuthenticationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// JWT ile mevcut kullanıcı use case; GetCurrentUserPort uygular.
@Service
@RequiredArgsConstructor
public class GetCurrentUserUseCase implements GetCurrentUserPort {

    private final AuthenticationDomainService authenticationDomainService;

    @Override
    public UserInfo execute(String token) {
        return authenticationDomainService.getCurrentUser(token);
    }
}
