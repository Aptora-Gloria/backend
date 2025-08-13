package com.oobeya.github.constant;

public class GitHubConstants {

    // === Base URL ===
    public static final String BASE_API_URL = "https://api.github.com";
    public static final String BASE_URL = "https://github.com";

    // === Endpoints ===
    public static final String USER_ENDPOINT = BASE_API_URL + "/user";
    public static final String USER_REPOS_ENDPOINT = BASE_API_URL + "/user/repos";
    public static final String REPO_COMMITS_ENDPOINT_TEMPLATE = BASE_API_URL + "/repos/%s/%s/commits";
    public static final String REPO_CONTENTS_ENDPOINT_TEMPLATE = BASE_API_URL + "/repos/%s/%s/contents/%s";
    public static final String USER_ORGS_ENDPOINT = BASE_API_URL + "/user/orgs";
    public static final String TOKEN_ENDPOINT = "/login/oauth/access_token";
    public static final String REPO_BRANCHES_ENDPOINT_TEMPLATE =
            "https://api.github.com/repos/%s/%s/branches?per_page=%d&page=%d";

    // === Error Messages ===
    public static final String ERROR_USER_NOT_FOUND = "User not found with login: ";
    public static final String ERROR_ORG_FETCH_FAILED = "Failed to fetch organizations from GitHub: ";
    public static final String ERROR_TOKEN_FETCH_FAILED = "GitHub token fetch failed: ";
    public static final String ERROR_USER_INFO_FAILED = "GitHub user info fetch failed: ";
    public static final String ERROR_REPO_FETCH_FAILED = "GitHub repo fetch failed: ";
    public static final String ERROR_REPO_CONTENTS_FAILED = "GitHub repo contents fetch failed: ";
    public static final String ERROR_COMMIT_FETCH_FAILED = "GitHub commit fetch failed: ";

    private GitHubConstants() {
        // Utility class – no instantiation
    }
}
