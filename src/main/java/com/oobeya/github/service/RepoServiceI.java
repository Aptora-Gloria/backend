package com.oobeya.github.service;

import com.oobeya.github.dto.request.RepoInitrequest;
import com.oobeya.github.dto.response.RepoDto;


import java.util.List;

public interface RepoServiceI {

    List<RepoInitrequest> initUserRepos(String authToken);
    List<RepoDto> getReposFromDb(String ownerLogin);
}
