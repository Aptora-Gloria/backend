package com.optimetric.auth.infrastructure.persistence.entity;

import com.optimetric.shared.domain.OwnerType;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "github_users"

)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GitHub API’den gelen kullanıcı ID’si
    @Column(name = "github_user_id", nullable = false,unique = true)
    private Long githubUserId;

    // GitHub kullanıcı adı
    @Column(name = "login", nullable = false, length = 100)
    private String login;

    // GitHub OAuth access token
    private String accessToken;

    // Case-insensitive arama için login'in küçük harf kopyası
    @Column(name = "login_ci", nullable = false, length = 100)
    private String loginCi;

    // Görünen ad
    @Column(name = "name", length = 200)
    private String name;

    // Profil fotoğrafı URL’si
    @Column(name = "avatar_url", length = 500)
    private String avatar_url;

    // GitHub profil URL’si
    @Column(name = "html_url", length = 500)
    private String htmlUrl;

    // Hesap tipi: USER veya ORG
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 16)
    private OwnerType ownerType;

    // Oluşturulma zamanı
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // Güncellenme zamanı
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Son GitHub veri senkronizasyon zamanı
    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    // Persist öncesi created_at, updated_at ve login_ci atanır.
    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (login != null) loginCi = login.toLowerCase();
    }

    // Update öncesi updated_at ve login_ci güncellenir.
    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
        if (login != null) loginCi = login.toLowerCase();
    }
}
