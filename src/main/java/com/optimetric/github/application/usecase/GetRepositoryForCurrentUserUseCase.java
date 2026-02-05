package com.optimetric.github.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.application.dto.UserInfo;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.port.out.RepositoryPersistencePort;
import com.optimetric.github.domain.service.RepositoryDomainService;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetRepositoryForCurrentUserUseCase {

    private final AuthServicePort authServicePort;
    private final RepositoryPersistencePort repositoryPersistencePort;
    private final RepositoryDomainService repositoryDomainService;

    public Optional<RepositoryInfo> execute(String login, String fullName) throws UserNotFoundException {
        // fullName formatını domain servisi ile doğrula (iş kuralı)
        repositoryDomainService.validateAndParseFullName(fullName);
        
        // Kullanıcı bilgisini al (Application DTO, entity değil)
        UserInfo userInfo = authServicePort.findByLoginOrThrow(login);
        
        // Önce fullName ve userId ile repoyu bul
        // Kullanıcının sync ettiği repolar (org repoları dahil)
        Optional<RepositoryInfo> repository = repositoryPersistencePort.findByFullNameAndUser(
                fullName, userInfo.id());
        
        // Kullanıcıyla bulunduysa erişim var (sync ettiği için)
        if (repository.isPresent()) {
            return repository;
        }
        
        // Kullanıcıyla bulunamadıysa sadece fullName ile dene (public repo vb.)
        repository = repositoryPersistencePort.findByFullName(fullName);
        
        // If repository not found, return empty
        if (repository.isEmpty()) {
            return Optional.empty();
        }
        
        // Apply domain-level access validation (business rule)
        // Sahiplik ve görünürlüğe göre erişim kontrolü
        // (e.g., public repos are accessible, private repos only to owner)
        if (!repositoryDomainService.canUserAccessRepository(login, repository.get())) {
            return Optional.empty();
        }
        
        return repository;
    }

    public RepositoryInfo executeOrThrow(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        return execute(login, fullName)
                .orElseThrow(() -> new RepositoryNotFoundException(
                        "Repository not found with fullName: " + fullName + 
                        " for user: " + login + 
                        ". Please ensure the repository is initialized first."));
    }
}
