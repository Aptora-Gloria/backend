package com.oobeya.github.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "github_pull_requests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_repo_pr_number",
                        columnNames = {"repository_id", "pr_number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestEntity {

    // ---------------------
    //  ENUM BURADA (inner)
    // ---------------------
    public enum PullRequestStatus {
        OPEN,
        CLOSED,
        MERGED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_pr_id")
    private Long githubPrId;

    @Column(name = "pr_number", nullable = false)
    private Integer number;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PullRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_branch_id", nullable = false)
    private BranchEntity sourceBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_branch_id", nullable = false)
    private BranchEntity targetBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CommitEntity> commits = new ArrayList<>();

    @Column(name = "html_url", length = 500)
    private String htmlUrl;

    @Column(name = "api_url", length = 500)
    private String apiUrl;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "merged_at")
    private OffsetDateTime mergedAt;
}
