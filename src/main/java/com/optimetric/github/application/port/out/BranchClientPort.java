package com.optimetric.github.application.port.out;

import java.util.List;

public interface BranchClientPort {
    List<String> getRepoBranchNames(String accessToken, String ownerLogin, String repoName);
}
