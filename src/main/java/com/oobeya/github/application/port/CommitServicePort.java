package com.oobeya.github.application.port;

import com.oobeya.github.api.response.CommitDto;

import java.util.List;

public interface CommitServicePort {
    
    List<CommitDto> initCommits(String login, String fullName, String branchName, String since);
    List<CommitDto> getCommits(String login, String fullName, String branchName);
}
