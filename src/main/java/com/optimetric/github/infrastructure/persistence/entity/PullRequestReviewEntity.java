package com.optimetric.github.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "github_pull_request_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_review_id", nullable = false, unique = true)
    private Long githubReviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequestEntity pullRequest;

    @Column(name = "reviewer_login", length = 100)
    private String reviewerLogin;

    @Column(name = "reviewer_github_id")
    private Long reviewerGithubId;

    @Column(name = "state", length = 20)
    private String state; // APPROVED, CHANGES_REQUESTED, COMMENTED

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

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