package com.optimetric.auth;

import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.auth.infrastructure.persistence.jpa.repository.UserRepository;
import com.optimetric.shared.domain.OwnerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void itShouldFindUserByLogin() {
        // verilen
        var now = OffsetDateTime.now();
        User user = User.builder()
                .githubUserId(12345L)
                .login("mehmetarif")
                .loginCi("mehmetarif")
                .name("Mehmet Arif")
                .avatar_url("https://github.com/avatar.png")
                .htmlUrl("https://github.com/mehmetarif")
                .ownerType(OwnerType.USER)
                .createdAt(now)
                .updatedAt(now)
                .lastSyncedAt(now)
                .build();

        userRepository.save(user);

        // işlem
        Optional<User> foundUser = userRepository.findByLogin("mehmetarif");

        // sonuç
        assertTrue(foundUser.isPresent());
        assertEquals("Mehmet Arif", foundUser.get().getName());
        assertEquals("mehmetarif", foundUser.get().getLogin());
    }
}
