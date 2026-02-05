package com.optimetric.github.application.port;

import com.optimetric.github.application.dto.SyncJobInfo;
import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;

import java.util.List;

public interface SyncJobServicePort {

    void executePrListJob(String repoFullName);

    void executePrDetailJob(String repoFullName, Long pullRequestId);

    void executePrReviewsJob(String repoFullName, Long pullRequestId);

    void executePrCommitsJob(String repoFullName, Long pullRequestId);

    SyncJobInfo createJob(SyncJobType jobType, String repoFullName);

    void updateJobStatus(Long jobId, SyncJobStatus status, String errorMessage);

    List<SyncJobInfo> getPendingJobs();

    List<SyncJobInfo> getJobsForRepository(String repoFullName);

    SyncJobInfo getLatestJobForRepository(String repoFullName, SyncJobType jobType);
}