package com.oobeya.github.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "github_repository_languages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Dil adı (örneğin Java, Python, HTML)
    @Column(name = "language_name", length = 100, nullable = false)
    private String languageName;

    // 🔹 GitHub API'nin verdiği byte miktarı
    @Column(name = "bytes", nullable = false)
    private Long bytes;

    // 🔹 Hesaplanan oran (örneğin toplam 100.000 byte içinde %74.1 Java)
    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    // 🔹 İlişki: 1 repo -> N language
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    // 🔹 Son güncelleme (API'den çekilen veri tarihi)
    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;
}
