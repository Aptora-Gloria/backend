package com.oobeya.github.application.port;

import com.oobeya.github.api.response.CommitDto;

import java.util.List;

public interface CommitServicePort {
    
    List<CommitDto> fetchAndSaveCommits(String authHeader, String fullName, String branchName, String since);
    List<CommitDto> getCommitsByFullName(String authHeader, String fullName);
    List<CommitDto> getCommitsByFullNameAndBranch(String authHeader, String fullName, String branchName);
}
