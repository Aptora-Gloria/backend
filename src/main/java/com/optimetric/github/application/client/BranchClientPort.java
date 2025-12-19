package com.optimetric.github.application.client;

import java.util.List;

public interface BranchClientPort {
    List<String> getRepoBranchNames(String accessToken, String ownerLogin, String repoName);
}

