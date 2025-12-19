package com.optimetric.github.application.port;

import com.optimetric.github.api.response.BranchDto;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

public interface BranchServicePort {

    List<BranchDto> initRepoBranches(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
    
    List<BranchDto> getBranchesByFullName(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
}
