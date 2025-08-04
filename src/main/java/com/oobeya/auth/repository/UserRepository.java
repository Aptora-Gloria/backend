package com.oobeya.auth.repository;

import com.oobeya.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User Entity için CRUD işlemlerini sağlar.
 * Spring Data JPA sayesinde metot yazmadan otomatik olarak SQL üretir.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login); // GitHub login ile kullanıcıyı getir
}
