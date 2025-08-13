package com.oobeya.github.dto.request;

import java.util.List;

public record BranchInitRequest(
        String repoHtmlUrl,
        List<String> branchNames
) {}
