package com.optimetric.github.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(
        name = "github_pull_requests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_github_pull_requests_repo_github_pr_id",
                        columnNames = {"repository_id", "github_pr_id"}
                ),
                @UniqueConstraint(
                        name = "uk_github_pull_requests_repo_number",
                        columnNames = {"repository_id", "number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_pr_id", nullable = false)
    private Long githubPrId;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "state", nullable = false, length = 50)
    private String state; // open, closed

    @Column(name = "locked", nullable = false)
    private boolean locked;

    @Column(name = "is_draft", nullable = false)
    private boolean draft;

    @Column(name = "is_merged", nullable = false)
    private boolean merged;

    @Column(name = "merge_commit_sha", length = 100)
    private String mergeCommitSha;

    @Column(name = "html_url", length = 500)
    private String htmlUrl;

    @Column(name = "diff_url", length = 500)
    private String diffUrl;

    @Column(name = "patch_url", length = 500)
    private String patchUrl;

    // Author snapshot
    @Column(name = "author_github_id")
    private Long authorGithubId;

    @Column(name = "author_login", length = 100)
    private String authorLogin;

    // Branch snapshot
    @Column(name = "head_ref", length = 200)
    private String headRef;

    @Column(name = "head_sha", length = 100)
    private String headSha;

    @Column(name = "base_ref", length = 200)
    private String baseRef;

    @Column(name = "base_sha", length = 100)
    private String baseSha;

    // Metrics-oriented fields
    @Column(name = "commit_count")
    private Integer commitCount;

    @Column(name = "additions")
    private Integer additions;

    @Column(name = "deletions")
    private Integer deletions;

    @Column(name = "changed_files")
    private Integer changedFiles;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "review_comment_count")
    private Integer reviewCommentCount;

    // Times
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "merged_at")
    private OffsetDateTime mergedAt;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "repository_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pull_requests_repository")
    )
    private RepositoryEntity repository;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "github_pull_request_commits",
            joinColumns = @JoinColumn(name = "pull_request_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "commit_id", referencedColumnName = "id")
    )
    private Set<CommitEntity> commits;
}

