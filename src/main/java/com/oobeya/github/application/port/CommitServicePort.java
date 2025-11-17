package com.oobeya.github.application.port;

import com.oobeya.github.api.response.CommitDto;

import java.util.List;

public interface CommitServicePort {
    
    List<CommitDto> initCommits(String login, String fullName, String branchName, String since);
    List<CommitDto> getCommitsByFullName(String login, String fullName);
    List<CommitDto> getCommitsByFullNameAndBranch(String login, String fullName, String branchName);
}
