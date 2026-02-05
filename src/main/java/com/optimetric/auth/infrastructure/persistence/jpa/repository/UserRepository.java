package com.optimetric.auth.infrastructure.persistence.jpa.repository;

import com.optimetric.auth.infrastructure.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login); // GitHub login ile kullanıcıyı getir
    Optional<User> findByGithubUserId(Long githubUserId);
}
