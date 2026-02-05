package com.optimetric.github.application.usecase;

import com.optimetric.github.application.dto.CommitInfo;
import com.optimetric.github.application.port.in.GetCommitsPort;
import com.optimetric.github.application.port.out.CommitQueryPort;
import com.optimetric.shared.exception.BranchNotFoundException;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCommitsUseCase implements GetCommitsPort {

    private final CommitQueryPort commitQueryPort;

    @Override
    public List<CommitInfo> getCommits(String login, String fullName, String branchName)
            throws UserNotFoundException, RepositoryNotFoundException, BranchNotFoundException {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("login must not be blank");
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("fullName must not be blank");
        return commitQueryPort.getCommits(login, fullName, branchName);
    }
}
