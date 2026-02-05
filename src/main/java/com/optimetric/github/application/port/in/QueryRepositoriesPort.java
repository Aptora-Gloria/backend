package com.optimetric.github.application.port.in;

import java.util.List;

import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.shared.domain.OwnerType;
import com.optimetric.shared.exception.UserNotFoundException;

public interface QueryRepositoriesPort {

    List<RepositoryInfo> execute(String login, OwnerType ownerType, String ownerLogin) throws UserNotFoundException;
}
