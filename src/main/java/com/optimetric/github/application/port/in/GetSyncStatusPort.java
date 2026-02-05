package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.SyncStatusInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetSyncStatusPort {

    SyncStatusInfo getSyncStatus(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException;
}
