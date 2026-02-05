package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.CommitTimeAnalysisInfo;
import com.optimetric.metrics.application.port.in.GetCommitTimeAnalysisPort;
import com.optimetric.metrics.application.port.out.UserMetricsDataPort;
import com.optimetric.metrics.domain.model.CommitMetricsResult;
import com.optimetric.metrics.domain.service.CommitMetricsCalculator;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCommitTimeAnalysisUseCase implements GetCommitTimeAnalysisPort {

    private final UserMetricsDataPort userMetricsDataPort;

    @Override
    public CommitTimeAnalysisInfo getCommitTimeAnalysis(String login) throws UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        var input = userMetricsDataPort.getCommitMetricsInput(login.trim());
        CommitMetricsResult result = CommitMetricsCalculator.calculate(input);
        return toInfo(result);
    }

    private static CommitTimeAnalysisInfo toInfo(CommitMetricsResult r) {
        return new CommitTimeAnalysisInfo(
                r.commitsByHour(), r.commitsByDayOfWeek(), r.weekdayCommits(), r.weekendCommits(),
                r.weekdayPercentage(), r.weekendPercentage(), r.mostActiveHour(), r.mostActiveDay(),
                r.commitsLast30Days(), r.commitsLast7Days()
        );
    }
}
