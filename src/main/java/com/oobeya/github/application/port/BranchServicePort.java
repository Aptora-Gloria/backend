package com.oobeya.github.application.port;



import com.oobeya.github.api.response.BranchDto;

import java.util.List;

public interface BranchServicePort {


    List<BranchDto> initRepoBranches(String authToken, long repo_id);


    List<BranchDto> getBranchesFromDb(long repo_id);
}
