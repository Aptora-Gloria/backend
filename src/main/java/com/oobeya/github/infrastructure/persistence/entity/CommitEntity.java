package com.oobeya.github.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "github_repository_commits",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_commit_sha_branch",
            columnNames = {"sha", "branch_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id")
    private PullRequestEntity pullRequest;

    // 🔹 Git commit hash
    @Column(name = "sha", length = 40, nullable = false)
    private String sha;

    @Column(name = "node_id", length = 200)
    private String nodeId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    // 🔹 Git commit metadata - author
    @Column(name = "author_name", length = 200)
    private String authorName;

    @Column(name = "author_email", length = 255)
    private String authorEmail;

    @Column(name = "authored_at")
    private OffsetDateTime authoredAt;

    // 🔹 Git commit metadata - committer
    @Column(name = "committer_name", length = 200)
    private String committerName;

    @Column(name = "committer_email", length = 255)
    private String committerEmail;

    @Column(name = "committed_at")
    private OffsetDateTime committedAt;

    // 🔹 GitHub user login'leri (outer author/committer)
    @Column(name = "author_login", length = 100)
    private String authorLogin;

    @Column(name = "committer_login", length = 100)
    private String committerLogin;

    // 🔹 Merge commit mi? (parents.length > 1)
    @Column(name = "is_merge_commit")
    private Boolean mergeCommit;

    // 🔹 Linkler
    @Column(name = "html_url", length = 500)
    private String htmlUrl;

    @Column(name = "api_url", length = 500)
    private String apiUrl;

    // 🔹 İlişkiler
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchEntity branch;
}
