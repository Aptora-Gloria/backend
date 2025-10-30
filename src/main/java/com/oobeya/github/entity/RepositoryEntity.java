package com.oobeya.github.entity;

import com.oobeya.auth.entity.OwnerType;
import com.oobeya.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "repositories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GitHub repo ID
    @Column(name = "github_repo_id", nullable = false, unique = true)
    private Long githubRepoId;

    // Repo temel bilgileri
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;  // owner/repo

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;


    @Column(name = "html_url", length = 500)
    private String htmlUrl;

    // Metrik verileri (PERFORMANCE İÇİN ÖNEMLİ!)
    @Column(name = "is_private")
    private Boolean isPrivate;

    @Column(name = "is_fork")
    private Boolean isFork;  // Fork'lar metrikte farklı değerlendirilir

    @Column(name = "stargazers_count")
    private Integer stargazersCount;  // Topluluk ilgisi

    @Column(name = "watchers_count")
    private Integer watchersCount;

    @Column(name = "forks_count")
    private Integer forksCount;  // Repo etkisi

    @Column(name = "open_issues_count")
    private Integer openIssuesCount;

    @Column(name = "size")
    private Integer size;  // KB cinsinden

    // Teknik detaylar
    @Column(name = "language", length = 50)
    private String language;  // Ana dil (Java, Dart, etc.)

    @Column(name = "default_branch", length = 50)
    private String defaultBranch;

    // Tarihler (Aktivite metrikleri için)
    @Column(name = "created_at")
    private OffsetDateTime createdAt;  // Repo oluşturma

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;  // Son güncelleme

    @Column(name = "pushed_at")
    private OffsetDateTime pushedAt;  // Son commit

    // Owner bilgisi (Organization vs User)
    @Column(name = "owner_login", nullable = false, length = 100)
    private String ownerLogin;

    @Column(name = "owner_type", length = 20)
    @Enumerated(EnumType.STRING)
    private OwnerType ownerType;  // USER, ORGANIZATION

    // User ile ilişki
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",referencedColumnName = "id", nullable = false)
    private User user;

    // Bizim sistemimiz
    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    @Column(name = "system_created_at", nullable = false)
    private OffsetDateTime systemCreatedAt;

    @Column(name = "system_updated_at", nullable = false)
    private OffsetDateTime systemUpdatedAt;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        systemCreatedAt = now;
        systemUpdatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        systemUpdatedAt = OffsetDateTime.now();
    }
}