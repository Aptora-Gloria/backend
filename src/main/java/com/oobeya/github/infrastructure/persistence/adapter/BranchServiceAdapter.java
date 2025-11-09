package com.oobeya.github.infrastructure.persistence.adapter;


import com.oobeya.github.api.response.BranchDto;
import com.oobeya.github.infrastructure.persistence.entity.BranchEntity;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;

import com.oobeya.github.infrastructure.core.mapper.BranchMapper;
import com.oobeya.github.infrastructure.persistence.jpa.BranchRepository;
import com.oobeya.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.BranchServicePort;
import com.oobeya.github.infrastructure.util.BranchUid;
import com.oobeya.auth.application.AuthServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceAdapter implements BranchServicePort {

    private final BranchRepository branchRepository;
    private final RepositoryRepository repositoryRepository;
    private final ApiClientPort apiClient;
    private final AuthServicePort authServicePort;

    @Override
    @Transactional
    public List<BranchDto> initRepoBranches(String authToken, String repoHtmlUrl) {
        String accessToken = authServicePort.getAccessTokenFromToken(authToken);

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
