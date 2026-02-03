package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

/**
 * Inbound port: initialize user repositories from GitHub and persist.
 */
public interface InitializeUserRepositoriesPort {

    List<RepositoryInitData> execute(String login) throws UserNotFoundException;
}
