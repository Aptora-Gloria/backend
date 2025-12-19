package com.optimetric.github.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "github_pull_request_commits",
       uniqueConstraints = @UniqueConstraint(columnNames = {"github_repo_id", "sha"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestCommitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_repo_id", nullable = false)
    private Long githubRepoId;

    @Column(name = "sha", nullable = false, length = 40)
    private String sha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequestEntity pullRequest;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "author_login", length = 100)
    private String authorLogin;

    @Column(name = "author_name", length = 200)
    private String authorName;

    @Column(name = "author_email", length = 200)
    private String authorEmail;

    @Column(name = "committed_at")
    private OffsetDateTime committedAt;

    @Column(name = "html_url", length = 500)
    private String htmlUrl;

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