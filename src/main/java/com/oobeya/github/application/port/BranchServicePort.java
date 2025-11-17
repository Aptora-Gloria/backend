package com.oobeya.github.application.port;

import com.oobeya.github.api.response.BranchDto;

import java.util.List;

public interface BranchServicePort {

    List<BranchDto> initRepoBranches(String login, String fullName);
    List<BranchDto> getBranchesByFullName(String login, String fullName);
}
