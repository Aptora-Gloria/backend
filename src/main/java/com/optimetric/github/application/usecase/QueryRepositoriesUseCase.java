package com.optimetric.github.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.application.dto.UserInfo;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.port.in.QueryRepositoriesPort;
import com.optimetric.github.application.port.out.RepositoryPersistencePort;
import com.optimetric.github.domain.service.RepositoryDomainService;
import com.optimetric.shared.domain.OwnerType;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueryRepositoriesUseCase implements QueryRepositoriesPort {

    private final AuthServicePort authServicePort;
    private final RepositoryPersistencePort repositoryPersistencePort;
    private final RepositoryDomainService repositoryDomainService;

    @Override
    public List<RepositoryInfo> execute(String login, OwnerType ownerType, String ownerLogin) 
            throws UserNotFoundException {
        // Kullanıcı bilgisini al (Application DTO, entity değil)
        UserInfo userInfo = authServicePort.findByLoginOrThrow(login);

        // userId ile kriterlere göre repoları sorgula (entity değil)
        List<RepositoryInfo> repositories = repositoryPersistencePort.findByUserWithCriteria(
                userInfo.id(), ownerType, ownerLogin);

        // Gerekirse domain filtrelemesi uygula (iş kuralı)
        // Filtreleme şu an persistence katmanında; domain servisi genişletilebilir
        // Ek iş kuralı doğrulaması için kullanılabilir
        return repositories;
    }
}
