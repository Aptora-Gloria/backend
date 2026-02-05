package com.optimetric.github.domain.service;

import com.optimetric.github.domain.enumeration.DataLevel;

public final class DataLevelPolicy {

    private DataLevelPolicy() {}

    public static DataLevel resolveDataLevel(boolean hasChangeStats, boolean hasReviews, boolean hasCommits) {
        if (hasChangeStats && (hasReviews || hasCommits)) {
            return DataLevel.FULL;
        }
        if (hasChangeStats) {
            return DataLevel.FULL;
        }
        return DataLevel.BASIC;
    }

    public static DataLevel resolveDataLevel(boolean hasChangeStats) {
        return hasChangeStats ? DataLevel.FULL : DataLevel.BASIC;
    }
}
