package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.BranchInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

/**
 * Inbound port: initialize branches for a repository from GitHub.
 */
public interface InitBranchesPort {

    List<BranchInfo> initBranches(String login, String fullName)
            throws UserNotFoundException, RepositoryNotFoundException;
}
