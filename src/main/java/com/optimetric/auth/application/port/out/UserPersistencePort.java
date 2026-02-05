package com.optimetric.auth.application.port.out;

import com.optimetric.auth.application.dto.UserInfo;

import java.util.Optional;

// Kullanıcı kalıcılık outbound portu; veritabanı erişimini soyutlar.
public interface UserPersistencePort {

    // Kullanıcı kaydeder veya günceller.
    UserInfo save(UserInfo userInfo);

    // Login ile kullanıcı arar.
    Optional<UserInfo> findByLogin(String login);

    // GitHub kullanıcı id ile kullanıcı arar.
    Optional<UserInfo> findByGithubUserId(Long githubUserId);

    // Dahili id ile kullanıcı arar; diğer modüller için.
    Optional<UserInfo> findById(Long id);
}
