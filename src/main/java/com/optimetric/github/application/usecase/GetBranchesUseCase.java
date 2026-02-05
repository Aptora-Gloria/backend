package com.optimetric.github.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.optimetric.github.application.dto.BranchInfo;
import com.optimetric.github.application.port.in.GetBranchesPort;
import com.optimetric.github.application.port.out.BranchQueryPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetBranchesUseCase implements GetBranchesPort {

    private final BranchQueryPort branchQueryPort;

    @Override
    public List<BranchInfo> getBranches(String login, String fullName)
            throws UserNotFoundException, RepositoryNotFoundException {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("login must not be blank");
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("fullName must not be blank");
        return branchQueryPort.getBranchesByFullName(login, fullName);
    }
}
