package com.optimetric.github.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.optimetric.auth.infrastructure.persistence.entity.OwnerType;

public record GetReposRequest(
        OwnerType ownerType,
        String ownerLogin
) {
    @JsonCreator
    public GetReposRequest(
            @JsonProperty("ownerType") String ownerType,
            @JsonProperty("ownerLogin") String ownerLogin
    ) {
        this(
                parseOwnerType(ownerType),
                parseOwnerLogin(ownerLogin)
        );
    }

    private static OwnerType parseOwnerType(String ownerType) {
        if (ownerType == null || ownerType.isBlank()) {
            return null;
        }
        try {
            return OwnerType.valueOf(ownerType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String parseOwnerLogin(String ownerLogin) {
        if (ownerLogin == null || ownerLogin.isBlank()) {
            return null;
        }
        return ownerLogin;
    }
}

