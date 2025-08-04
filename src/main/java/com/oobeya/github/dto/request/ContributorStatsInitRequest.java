package com.oobeya.github.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContributorStatsInitRequest {
    private String owner;
    private String repo;
}
