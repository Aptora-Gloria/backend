package com.oobeya.github.application.port;

import com.oobeya.github.api.request.RepoInitrequest;
import com.oobeya.github.api.response.RepoDto;


import java.util.List;

public interface RepoServicePort {

    List<RepoInitrequest> initUserRepos(String authToken);
    List<RepoDto> getReposFromDb(String ownerLogin);
}
