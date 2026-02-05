package com.optimetric.github.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.optimetric.github.application.dto.ReviewCommentInfo;
import com.optimetric.github.application.port.in.GetPullRequestCommitsPort;
import com.optimetric.github.application.port.out.PullRequestQueryPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPullRequestCommitsUseCase implements GetPullRequestCommitsPort {

    private final PullRequestQueryPort pullRequestQueryPort;

    @Override
    public List<ReviewCommentInfo> getPullRequestCommits(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("login must not be blank");
        if (repoFullName == null || repoFullName.isBlank()) throw new IllegalArgumentException("repoFullName must not be blank");
        if (prNumber == null) throw new IllegalArgumentException("prNumber must not be null");
        return pullRequestQueryPort.getPullRequestCommits(login, repoFullName, prNumber);
    }
}
