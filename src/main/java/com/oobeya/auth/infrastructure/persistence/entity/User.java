package com.oobeya.auth.infrastructure.persistence.entity;

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

    // GitHub kullanıcı adı (örnek: arifferarslan)
    @Column(name = "login", nullable = false, length = 100)
    private String login;

    //github acesstoken
    private String accessToken;

    // Case-insensitive kıyaslama için küçük harf versiyonu
    @Column(name = "login_ci", nullable = false, length = 100)
    private String loginCi;

    // Kullanıcının tam adı (örnek: Arif Erarslan)
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

    // Kayıt oluşturulma zamanı (bizim sistemimizde)
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // Güncellenme zamanı
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // GitHub’dan en son ne zaman veri çekildi
    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    // Veritabanına ilk eklenmeden önce otomatik değer atama
    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (login != null) loginCi = login.toLowerCase();
    }

    // Güncelleme öncesinde tarih ve login_ci güncellemesi
    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
        if (login != null) loginCi = login.toLowerCase();
    }
}
