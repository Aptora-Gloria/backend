package com.oobeya.github.service;



import com.oobeya.github.dto.response.BranchDto;

import java.util.List;

public interface BranchServiceI {


    List<BranchDto> initRepoBranches(String authToken, String repoHtmlUrl);


    List<BranchDto> getBranchesFromDb(String repoHtmlUrl);
}
