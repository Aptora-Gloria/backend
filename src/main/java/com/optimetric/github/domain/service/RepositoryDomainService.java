package com.optimetric.github.domain.service;

import java.util.List;
import java.util.stream.Collectors;

import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.domain.vo.RepositoryFullName;
import com.optimetric.shared.domain.OwnerType;

public class RepositoryDomainService {

    public boolean isValidFullName(String fullName) {
        return RepositoryFullName.isValid(fullName);
    }

    public List<RepositoryInfo> filterByCriteria(
            List<RepositoryInfo> repositories,
            OwnerType ownerType,
            String ownerLogin) {
        
        return repositories.stream()
                .filter(repo -> {
                    // Filter by owner type if specified
                    if (ownerType != null && repo.ownerType() != ownerType) {
                        return false;
                    }
                    
                    // Filter by owner login if specified
                    if (ownerLogin != null && !ownerLogin.equals(repo.ownerLogin())) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }

    public boolean canUserAccessRepository(String userLogin, RepositoryInfo repository) {
        // İş kuralı: kullanıcı yalnızca kendi repolarına erişebilir
        if (userLogin.equals(repository.ownerLogin())) {
            return true;
        }
        
        // Business rule: Public repositories are accessible to everyone
        if (Boolean.FALSE.equals(repository.isPrivate())) {
            return true;
        }
        
        // Business rule: Private repositories are only accessible to owner
        // Basitleştirilmiş kural; gerçek erişim kontrolü daha karmaşık olabilir
        return false;
    }

    public boolean shouldIncludeRepository(RepositoryInfo repository, boolean includePrivate) {
        if (Boolean.TRUE.equals(repository.isPrivate()) && !includePrivate) {
            return false;
        }
        return true;
    }

    public RepositoryFullName validateAndParseFullName(String fullName) {
        return new RepositoryFullName(fullName);
    }
}
