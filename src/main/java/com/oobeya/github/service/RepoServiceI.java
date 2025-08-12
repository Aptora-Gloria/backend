package com.oobeya.github.service;

import com.oobeya.github.dto.response.RepoDto;


import java.util.List;

public interface RepoServiceI {

    List<RepoDto> initUserRepos(String authToken,String ownerLogin);
    List<RepoDto> getReposFromDb(String ownerLogin);
}
