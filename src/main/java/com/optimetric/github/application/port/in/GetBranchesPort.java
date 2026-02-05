package com.optimetric.github.application.port.in;

import java.util.List;

import com.optimetric.github.application.dto.BranchInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetBranchesPort {

    List<BranchInfo> getBranches(String login, String fullName)
            throws UserNotFoundException, RepositoryNotFoundException;
}
