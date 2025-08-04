package com.oobeya.githubOrg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BranchDto {
    private String name;

    @JsonProperty("commit")
    private CommitInfo commit;

    @Data
    public static class CommitInfo {
        private String sha;
        private String url;
    }
}
