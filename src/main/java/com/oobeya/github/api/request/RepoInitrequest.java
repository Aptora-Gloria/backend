package com.oobeya.github.api.request;

public record RepoInitrequest(
        Long id,
        String node_id,
        String name,
        String full_name,
        Owner owner,
        String description,
        String html_url,
        Boolean private_,     // maps from "private" (use private_ because of keyword)
        Boolean fork,
        Integer stargazers_count,
        Integer watchers_count,
        Integer forks_count,
        Integer open_issues_count,
        Integer size,
        String language,
        String default_branch,
        String created_at,    // ISO date-time string from GitHub
        String updated_at,
        String pushed_at,
        Permissions permissions
) {
    public record Owner(String login, String node_id, String type) {}
    public record Permissions(Boolean admin, Boolean maintain, Boolean push, Boolean triage, Boolean pull) {}
}

