package com.optimetric.github.api.request;

public record RepoInitRequest(
        Long id,
        String node_id,
        String name,
        String full_name,
        Owner owner,
        String description,
        String html_url,
        Boolean private_,
        Boolean fork,
        Integer stargazers_count,
        Integer watchers_count,
        Integer forks_count,
        Integer open_issues_count,
        Integer size,
        String language,
        String default_branch,
        String created_at,
        String updated_at,
        String pushed_at,
        Permissions permissions
) {
    public record Owner(String login, String node_id, String type) {}
    public record Permissions(Boolean admin, Boolean maintain, Boolean push, Boolean triage, Boolean pull) {}
}
