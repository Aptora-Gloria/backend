package com.optimetric.github.application.port;

import com.optimetric.github.api.response.CommitDto;

import java.util.List;

public interface CommitServicePort {
    
    List<CommitDto> initCommits(String login, String fullName, String branchName, String since);
    List<CommitDto> getCommits(String login, String fullName, String branchName);
}
