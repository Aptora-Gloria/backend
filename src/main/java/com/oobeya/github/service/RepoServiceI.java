package com.oobeya.github.service;

import com.oobeya.github.dto.response.RepoDto;

import java.util.List;
//
public interface RepoServiceI {


    List<RepoDto> SaveAndFecthRepos(String jwtToken);
    List<RepoDto> getUserReposFromDb(String login);


}
