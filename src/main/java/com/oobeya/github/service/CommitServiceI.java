package com.oobeya.github.service;

import com.oobeya.github.dto.CommitDto;

import java.util.List;

public interface CommitServiceI {
    List<CommitDto> fecthAndSaveCommits(String author, String owner, String repo);
    List<CommitDto> getCommitsFromDb(String author,String repo);
}
