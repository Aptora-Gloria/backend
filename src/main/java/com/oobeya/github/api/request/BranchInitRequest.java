package com.oobeya.github.api.request;

import java.util.List;

public record BranchInitRequest(
        String repoHtmlUrl,
        List<String> branchNames
) {}
