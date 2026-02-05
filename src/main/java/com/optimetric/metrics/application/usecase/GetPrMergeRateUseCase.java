package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.PrMergeRateInfo;
import com.optimetric.metrics.application.port.in.GetPrMergeRatePort;
import com.optimetric.metrics.application.port.out.UserMetricsDataPort;
import com.optimetric.metrics.domain.model.PrMetricsResult;
import com.optimetric.metrics.domain.service.PrMetricsCalculator;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GetPrMergeRateUseCase implements GetPrMergeRatePort {

    private final UserMetricsDataPort userMetricsDataPort;

    @Override
    public PrMergeRateInfo getPrMergeRate(String login) throws UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        var input = userMetricsDataPort.getPrMetricsInput(login.trim());
        PrMetricsResult result = PrMetricsCalculator.calculate(input);
        return toInfo(result);
    }

    private static PrMergeRateInfo toInfo(PrMetricsResult r) {
        return new PrMergeRateInfo(
                r.totalPullRequests(), r.mergedPullRequests(), r.openPullRequests(), r.closedPullRequests(),
                r.mergeRate(), r.averageMergeTimeHours() != null ? r.averageMergeTimeHours() : BigDecimal.ZERO
        );
    }
}
