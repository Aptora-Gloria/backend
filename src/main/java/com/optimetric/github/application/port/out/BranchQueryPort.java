package com.optimetric.github.application.port.out;

import java.util.List;

import com.optimetric.github.application.dto.BranchInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface BranchQueryPort {

    List<BranchInfo> initRepoBranches(String login, String fullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    List<BranchInfo> getBranchesByFullName(String login, String fullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
