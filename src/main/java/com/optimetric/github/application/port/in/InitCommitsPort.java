package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.CommitInfo;
import com.optimetric.shared.exception.BranchNotFoundException;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

/**
 * Inbound port: initialize commits for a repository/branch from GitHub.
 */
public interface InitCommitsPort {

    List<CommitInfo> initCommits(String login, String fullName, String branchName, String since)
            throws UserNotFoundException, RepositoryNotFoundException, BranchNotFoundException;
}
