package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.RepositoryCodeQualityInfo;
import com.optimetric.metrics.application.port.in.GetCodeQualityPort;
import com.optimetric.metrics.application.port.out.RepositoryMetricsQueryPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCodeQualityUseCase implements GetCodeQualityPort {

    private final RepositoryMetricsQueryPort repositoryMetricsQueryPort;

    @Override
    public RepositoryCodeQualityInfo getCodeQuality(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        if (repoFullName == null || repoFullName.isBlank()) {
            throw new IllegalArgumentException("repoFullName cannot be null or blank");
        }
        return repositoryMetricsQueryPort.getCodeQualityInfo(login, repoFullName.trim());
    }
}
