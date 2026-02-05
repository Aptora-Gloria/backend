package com.optimetric.github.application.port.in;

import java.util.List;

import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.shared.exception.UserNotFoundException;

public interface InitializeUserRepositoriesPort {

    List<RepositoryInitData> execute(String login) throws UserNotFoundException;
}
