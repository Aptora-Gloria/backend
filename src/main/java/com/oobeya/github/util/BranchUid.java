package com.oobeya.github.util;

public final class BranchUid {

    private BranchUid() {}

    public static String of( String branchName,String repoHtmlUrl) {
        return repoHtmlUrl.trim().toLowerCase() + "::" + branchName.trim().toLowerCase();
    }
}
