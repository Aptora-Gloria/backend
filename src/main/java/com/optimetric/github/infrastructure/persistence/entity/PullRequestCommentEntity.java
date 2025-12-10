package com.optimetric.github.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "github_pull_request_comments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pull_request_comments_pr_github_comment_id",
                        columnNames = {"pull_request_id", "github_comment_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_comment_id", nullable = false)
    private Long githubCommentId;

    @Column(name = "author_github_id")
    private Long authorGithubId;

    @Column(name = "author_login", length = 100)
    private String authorLogin;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "pull_request_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pull_request_comments_pull_request")
    )
    private PullRequestEntity pullRequest;
}

