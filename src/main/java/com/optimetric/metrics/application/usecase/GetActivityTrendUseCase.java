package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.ActivityTrendInfo;
import com.optimetric.metrics.application.port.in.GetActivityTrendPort;
import com.optimetric.metrics.application.port.out.UserMetricsDataPort;
import com.optimetric.metrics.domain.model.ActivityTrendResult;
import com.optimetric.metrics.domain.service.ActivityTrendCalculator;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetActivityTrendUseCase implements GetActivityTrendPort {

    private final UserMetricsDataPort userMetricsDataPort;

    @Override
    public ActivityTrendInfo getActivityTrend(String login) throws UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        var input = userMetricsDataPort.getActivityTrendInput(login.trim());
        ActivityTrendResult result = ActivityTrendCalculator.calculate(input);
        return toInfo(result);
    }

    private static ActivityTrendInfo toInfo(ActivityTrendResult r) {
        List<ActivityTrendInfo.WeeklyActivityInfo> weekly = r.weeklyTrend() == null
                ? List.of()
                : r.weeklyTrend().stream()
                        .map(w -> new ActivityTrendInfo.WeeklyActivityInfo(w.week(), w.commits(), w.pullRequests(), w.reviews(), w.totalActivity()))
                        .toList();
        List<ActivityTrendInfo.MonthlyActivityInfo> monthly = r.monthlyTrend() == null
                ? List.of()
                : r.monthlyTrend().stream()
                        .map(m -> new ActivityTrendInfo.MonthlyActivityInfo(m.month(), m.commits(), m.pullRequests(), m.reviews(), m.totalActivity()))
                        .toList();
        ActivityTrendInfo.ActivityBreakdownInfo breakdown = r.last30DaysBreakdown() == null
                ? null
                : new ActivityTrendInfo.ActivityBreakdownInfo(
                r.last30DaysBreakdown().commits(), r.last30DaysBreakdown().pullRequests(), r.last30DaysBreakdown().reviews(),
                r.commitsByDay(), r.prsByDay()
        );
        return new ActivityTrendInfo(weekly, monthly, r.totalActivityLast30Days(), r.totalActivityLast7Days(),
                r.activityGrowthRate(), r.mostActiveWeek(), r.mostActiveMonth(), breakdown);
    }
}
