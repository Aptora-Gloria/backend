package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.CommitInfo;
import com.optimetric.shared.exception.BranchNotFoundException;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

public interface GetCommitsPort {

    List<CommitInfo> getCommits(String login, String fullName, String branchName)
            throws UserNotFoundException, RepositoryNotFoundException, BranchNotFoundException;
}
