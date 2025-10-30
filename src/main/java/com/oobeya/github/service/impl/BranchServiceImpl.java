package com.oobeya.github.service.impl;


import com.oobeya.github.dto.response.BranchDto;
import com.oobeya.github.entity.BranchEntity;
import com.oobeya.github.entity.RepositoryEntity;

import com.oobeya.github.mapper.BranchMapper;
import com.oobeya.github.repository.BranchRepository;
import com.oobeya.github.repository.RepositoryRepository;
import com.oobeya.github.service.ApiClientI;
import com.oobeya.github.service.BranchServiceI;
import com.oobeya.github.util.BranchUid;
import com.oobeya.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchServiceI {

    private final BranchRepository branchRepository;
    private final RepositoryRepository repositoryRepository;
    private final ApiClientI apiClient;
    private final AuthService authService;

    @Override
    @Transactional
    public List<BranchDto> initRepoBranches(String authToken, String repoHtmlUrl) {
        String accessToken = authService.getAccessTokenFromToken(authToken);

        RepositoryEntity repo = repositoryRepository.findByHtmlUrl(repoHtmlUrl)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found by htmlUrl: " + repoHtmlUrl));

        String owner = repo.getOwnerLogin(); // <<< tek fark burası
        List<String> branchNames = apiClient.getRepoBranchNames(accessToken, owner, repo.getName());

        saveBranches(branchNames, repo);
        return getBranchesFromDb(repoHtmlUrl);
    }




    @Override
    @Transactional(readOnly = true)
    public List<BranchDto> getBranchesFromDb(String repoHtmlUrl) {
        return branchRepository.findAllByRepository_HtmlUrl(repoHtmlUrl)
                .stream()
                .map(BranchMapper::toDto)
                .toList();
    }


    private void saveBranches(List<String> branchNames, RepositoryEntity repo) {
        if (branchNames == null || branchNames.isEmpty()) return;


        List<String> uids = branchNames.stream()
                .map(name -> BranchUid.of(repo.getHtmlUrl(), name))
                .toList();

        List<BranchEntity> existing = branchRepository.findAllByUidIn(uids);
        var existingUidSet = existing.stream().map(BranchEntity::getUid).collect(java.util.stream.Collectors.toSet());

        List<BranchEntity> toSave = new ArrayList<>();
        for (String name : branchNames) {
            String uid = BranchUid.of(repo.getHtmlUrl(), name);
            if (existingUidSet.contains(uid)) continue;

            BranchEntity e = BranchEntity.builder()
                    .name(name)
                    .repository(repo) // uid @PrePersist ile üretilecek
                    .build();
            toSave.add(e);
        }


        if (!toSave.isEmpty()) {
            branchRepository.saveAll(toSave);
        }
    }
}
