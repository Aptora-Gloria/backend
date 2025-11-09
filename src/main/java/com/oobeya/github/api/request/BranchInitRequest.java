package com.oobeya.github.api.request;

import java.util.List;

public record BranchInitRequest(
        long repo_id,
        List<String> branchNames
) {}
