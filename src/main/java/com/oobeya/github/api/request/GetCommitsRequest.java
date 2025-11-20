package com.oobeya.github.api.request;

import jakarta.validation.constraints.NotBlank;

public record GetCommitsRequest(
        @NotBlank(message = "fullName is required")
        String fullName,
        
        String branchName
) {}

