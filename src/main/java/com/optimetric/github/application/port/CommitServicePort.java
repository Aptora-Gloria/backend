package com.optimetric.github.application.port;

import com.optimetric.github.api.response.CommitDto;
import com.optimetric.shared.exception.BranchNotFoundException;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

public interface CommitServicePort {
    
    List<CommitDto> initCommits(String login, String fullName, String branchName, String since) 
            throws RepositoryNotFoundException, BranchNotFoundException, UserNotFoundException;
    
    List<CommitDto> getCommits(String login, String fullName, String branchName) 
            throws RepositoryNotFoundException, BranchNotFoundException, UserNotFoundException;
}
