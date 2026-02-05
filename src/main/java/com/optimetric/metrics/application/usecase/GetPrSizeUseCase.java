package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.PrSizeInfo;
import com.optimetric.metrics.application.port.in.GetPrSizePort;
import com.optimetric.metrics.application.port.out.UserMetricsDataPort;
import com.optimetric.metrics.domain.model.PrMetricsResult;
import com.optimetric.metrics.domain.service.PrMetricsCalculator;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GetPrSizeUseCase implements GetPrSizePort {

    private final UserMetricsDataPort userMetricsDataPort;

    @Override
    public PrSizeInfo getPrSize(String login) throws UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        var input = userMetricsDataPort.getPrMetricsInput(login.trim());
        PrMetricsResult result = PrMetricsCalculator.calculate(input);
        return toInfo(result);
    }

    private static PrSizeInfo toInfo(PrMetricsResult r) {
        return new PrSizeInfo(
                r.averageAdditions() != null ? r.averageAdditions() : BigDecimal.ZERO,
                r.averageDeletions() != null ? r.averageDeletions() : BigDecimal.ZERO,
                r.averageChangedFiles() != null ? r.averageChangedFiles() : BigDecimal.ZERO,
                r.averageTotalChanges() != null ? r.averageTotalChanges() : BigDecimal.ZERO,
                r.smallPrCount(), r.mediumPrCount(), r.largePrCount(),
                r.smallPrPercentage() != null ? r.smallPrPercentage() : BigDecimal.ZERO,
                r.mediumPrPercentage() != null ? r.mediumPrPercentage() : BigDecimal.ZERO,
                r.largePrPercentage() != null ? r.largePrPercentage() : BigDecimal.ZERO
        );
    }
}
