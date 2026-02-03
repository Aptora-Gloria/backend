package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.BranchInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

/**
 * Inbound port: get branches for a repository.
 */
public interface GetBranchesPort {

    List<BranchInfo> getBranches(String login, String fullName)
            throws UserNotFoundException, RepositoryNotFoundException;
}
