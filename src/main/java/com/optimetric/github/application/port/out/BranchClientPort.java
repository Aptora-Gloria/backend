package com.optimetric.github.application.port.out;

import java.util.List;

/**
 * Outbound port for fetching repository branch names from GitHub API.
 */
public interface BranchClientPort {
    List<String> getRepoBranchNames(String accessToken, String ownerLogin, String repoName);
}
