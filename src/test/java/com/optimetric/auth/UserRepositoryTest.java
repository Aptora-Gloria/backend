/*package com.optimetric.auth;

import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void itShouldFindUserByLogin() {
        // given
        User user = User.builder()
                .login("mehmetarif")
                .email("mehmet@example.com")
                .avatarUrl("https://github.com/avatar.png")
                .build();

        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByLogin("mehmetarif");

        // then
        assertTrue(foundUser.isPresent());
        assertEquals("mehmet@example.com", foundUser.get().getEmail());
    }
}
*/