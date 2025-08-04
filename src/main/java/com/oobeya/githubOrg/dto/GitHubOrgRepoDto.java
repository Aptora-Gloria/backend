package com.oobeya.githubOrg.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubOrgRepoDto {

    private String name;
    private String fullName;
    private String htmlUrl;
    private String description;
    private boolean isPrivate;
    private boolean isFork;
    private String language;
    private int stargazersCount;
    private int forksCount;
    private String createdAt;
    private String updatedAt;
    private String organizationLogin;
}
