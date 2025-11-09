package com.oobeya.github.application.port;

import com.oobeya.github.api.response.CommitDto;

import java.util.List;

public interface CommitServicePort {
    List<CommitDto> fecthAndSaveCommits(String author, String owner, String repo);
    List<CommitDto> getCommitsFromDb(String author,String repo);
}
