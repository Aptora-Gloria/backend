package com.optimetric.github.application.port.out;

import com.optimetric.github.application.dto.CommitInfo;
import com.optimetric.shared.exception.BranchNotFoundException;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

/**
 * Outbound port for commit operations (init and query).
 */
public interface CommitQueryPort {

    List<CommitInfo> initCommits(String login, String fullName, String branchName, String since)
            throws RepositoryNotFoundException, BranchNotFoundException, UserNotFoundException;

    List<CommitInfo> getCommits(String login, String fullName, String branchName)
            throws RepositoryNotFoundException, BranchNotFoundException, UserNotFoundException;
}
