package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.shared.domain.OwnerType;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

/**
 * Inbound port: query repositories for user with optional filters.
 */
public interface QueryRepositoriesPort {

    List<RepositoryInfo> execute(String login, OwnerType ownerType, String ownerLogin) throws UserNotFoundException;
}
