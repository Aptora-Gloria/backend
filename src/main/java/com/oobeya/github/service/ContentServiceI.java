package com.oobeya.github.service;

import com.oobeya.github.dto.ContentDto;
import java.util.List;

public interface ContentServiceI {
    List<ContentDto> fectAndSaveContent(String author, String owner, String repo, String path);
    List<ContentDto> getContentFromDb(String author, String repo);
}
