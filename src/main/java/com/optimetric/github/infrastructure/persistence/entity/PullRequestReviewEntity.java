package com.optimetric.github.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "github_pull_request_reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pull_request_reviews_pr_github_review_id",
                        columnNames = {"pull_request_id", "github_review_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_review_id", nullable = false)
    private Long githubReviewId;

    @Column(name = "state", nullable = false, length = 50)
    private String state; // APPROVED, COMMENTED, CHANGES_REQUESTED, DISMISSED, PENDING

    @Column(name = "author_github_id")
    private Long authorGithubId;

    @Column(name = "author_login", length = 100)
    private String authorLogin;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "pull_request_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pull_request_reviews_pull_request")
    )
    private PullRequestEntity pullRequest;
}

