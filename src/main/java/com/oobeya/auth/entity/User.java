package com.oobeya.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * GitHub'dan gelen kullanıcı bilgilerini temsil eder ve veritabanına kaydedilir.
 */
@Entity
@Table(name = "github_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    private String email;

    private String avatarUrl;

    private String accessToken;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

