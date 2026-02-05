package com.optimetric.github.infrastructure.persistence.entity;

import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.domain.enumeration.DataLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "github_pull_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_pr_id", nullable = false, unique = true)
    private Long githubPrId;

    @Column(name = "pr_number", nullable = false)
    private Integer prNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_level", nullable = false)
    private DataLevel dataLevel;

    // Change statistics (nullable until PR_DETAIL job completes)
    @Column(name = "additions")
    private Integer additions;

    @Column(name = "deletions")
    private Integer deletions;

    @Column(name = "changed_files")
    private Integer changedFiles;

    @Column(name = "change_stats_synced_at")
    private OffsetDateTime changeStatsSyncedAt;

    // Standart PR alanları
    @Column(name = "state", length = 20)
    private String state; // open, closed

    @Column(name = "draft")
    private Boolean draft;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "author_login", length = 100)
    private String authorLogin;

    @Column(name = "author_github_id")
    private Long authorGithubId;

    @Column(name = "html_url", length = 500)
    private String htmlUrl;

    @Column(name = "merge_commit_sha", length = 40)
    private String mergeCommitSha;

    // GitHub'dan gelen zaman damgaları
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "merged_at")
    private OffsetDateTime mergedAt;

    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    // System timestamps
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