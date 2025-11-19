package com.oobeya.github.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "github_branches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PullRequestEntity {
    OPEN,
    CLOSED,
    MERGED
}


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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 GitHub PR id (global id)
    @Column(name = "github_pr_id")
    private Long githubPrId;

    // 🔹 GitHub pr number (repo içindeki sıra no: #1, #2, #3...)
    @Column(name = "pr_number", nullable = false)
    private Integer number;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PullRequestStatus status;

    // 🔹 Branch ilişkileri
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_branch_id", nullable = false)
    private BranchEntity sourceBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_branch_id", nullable = false)
    private BranchEntity targetBranch;

    // 🔹 PR bağlı olduğu repo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    // 🔹 PR içindeki commitler (istersen)
    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CommitEntity> commits = new ArrayList<>();

    // 🔹 Linkler
    @Column(name = "html_url", length = 500)
    private String htmlUrl;

    @Column(name = "api_url", length = 500)
    private String apiUrl;

    // 🔹 Tarihler
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "merged_at")
    private OffsetDateTime mergedAt;
