package com.optimetric.github.infrastructure.persistence.entity;

import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "github_sync_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private SyncJobType jobType;

    @Column(name = "repo_full_name", nullable = false, length = 255)
    private String repoFullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SyncJobStatus status;

    @Column(name = "cursor", length = 500)
    private String cursor; // Sayfalama için

    @Column(name = "current_page")
    private Integer currentPage;

    @Column(name = "attempt_count")
    private Integer attemptCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "next_run_at")
    private OffsetDateTime nextRunAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

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
        if (attemptCount == null) {
            attemptCount = 0;
        }
    }

    @PreUpdate
    void preUpdate() {
        systemUpdatedAt = OffsetDateTime.now();
    }
}