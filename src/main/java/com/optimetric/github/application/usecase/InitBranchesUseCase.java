package com.optimetric.github.application.usecase;

import com.optimetric.github.application.dto.BranchInfo;
import com.optimetric.github.application.port.in.InitBranchesPort;
import com.optimetric.github.application.port.out.BranchQueryPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case: initialize branches for a repository from GitHub.
 */
@Service
@RequiredArgsConstructor
public class InitBranchesUseCase implements InitBranchesPort {

    private final BranchQueryPort branchQueryPort;

    @Override
    public List<BranchInfo> initBranches(String login, String fullName)
            throws UserNotFoundException, RepositoryNotFoundException {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("login must not be blank");
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("fullName must not be blank");
        return branchQueryPort.initRepoBranches(login, fullName);
    }
}
