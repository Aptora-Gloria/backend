package com.oobeya.github.service.impl;


import com.oobeya.auth.service.AuthService;
import com.oobeya.github.dto.response.ContentDto;
import com.oobeya.github.entity.ContentEntity;
import com.oobeya.github.mapper.ContentMapper;
import com.oobeya.github.repository.ContentRepository;
import com.oobeya.github.service.ApiClientI;
import com.oobeya.github.service.ContentServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentServiceI {

    private final ApiClientI apiClient;//TODO base servise taşıncak
    private final AuthService authService;
    private final ContentRepository contentRepo;


    @Override
    public List<ContentDto> fectAndSaveContent(String authHeader, String owner, String repo, String path) {
        String accessToken = authService.getAccessTokenFromToken(authHeader);

        List<ContentDto> contents = apiClient.getRepoContents(accessToken, owner, repo, path != null ? path : "");
        saveContents(contents, repo);
        return contents;
    }

    @Override
    public List<ContentDto> getContentFromDb(String author, String repo) {
        return contentRepo.findByRepoName(repo).stream()
                .map(ContentMapper::toDto)
                .collect(Collectors.toList());//TODO mapper'a  taşıncak
    }



    public void saveContents(List<ContentDto> contents, String repoName) {//TODO entity buraya gelsin
        List<ContentEntity> entities = ContentMapper.toEntities(contents, repoName);
        contentRepo.saveAll(entities);
    }
}
