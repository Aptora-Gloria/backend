package com.optimetric.auth.application.port.in;

import com.optimetric.auth.application.dto.UserInfo;

// JWT ile mevcut kullanıcı almak için inbound port; use case uygular.
public interface GetCurrentUserPort {

    // JWT doğrular ve mevcut kullanıcı bilgisini döner.
    UserInfo execute(String token);
}
