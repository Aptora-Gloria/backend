package com.optimetric.github.application.port.out;

import com.optimetric.github.application.dto.BranchInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

/**
 * Outbound port for branch operations (init and query).
 */
public interface BranchQueryPort {

    List<BranchInfo> initRepoBranches(String login, String fullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    List<BranchInfo> getBranchesByFullName(String login, String fullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
