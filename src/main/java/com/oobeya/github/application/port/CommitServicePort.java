package com.oobeya.github.application.port;

import com.oobeya.github.api.response.CommitDto;

import java.util.List;

public interface CommitServicePort {
    
    /**
     * Fetches commits from GitHub API and saves them to database.
     * 
     * @param authHeader Authorization header
     * @param repositoryId Repository ID
     * @param branchId Branch ID
     * @return List of fetched commits
     */
    List<CommitDto> fetchAndSaveCommits(String authHeader, Long repositoryId, Long branchId);
    
    /**
     * Gets commits from database by repository ID.
     * 
     * @param repositoryId Repository ID
     * @return List of commits
     */
    List<CommitDto> getCommitsByRepositoryId(Long repositoryId);
    
    /**
     * Gets commits from database by branch ID.
     * 
     * @param branchId Branch ID
     * @return List of commits
     */
    List<CommitDto> getCommitsByBranchId(Long branchId);
    
    /**
     * Gets commits from database by repository ID and branch ID.
     * 
     * @param repositoryId Repository ID
     * @param branchId Branch ID
     * @return List of commits
     */
    List<CommitDto> getCommitsByRepositoryAndBranch(Long repositoryId, Long branchId);
}
