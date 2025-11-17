package com.oobeya.github.application.port;

import com.oobeya.github.api.response.BranchDto;

import java.util.List;

public interface BranchServicePort {

    List<BranchDto> initRepoBranches(String authToken, String fullName);
    List<BranchDto> getBranchesByFullName(String authToken, String fullName);
}
