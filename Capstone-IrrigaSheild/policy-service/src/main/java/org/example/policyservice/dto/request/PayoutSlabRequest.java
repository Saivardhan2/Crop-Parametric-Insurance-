package org.example.policyservice.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PayoutSlabRequest(

        @Min(
                value = 1,
                message = "Minimum duration must be positive"
        )
        Integer minimumDurationHours,

        @Min(
                value = 1,
                message = "Maximum duration must be positive"
        )
        Integer maximumDurationHours,

        @DecimalMin(
                value = "0.00",
                message = "Minimum availability must be at least 0"
        )
        @DecimalMax(
                value = "100.00",
                message = "Minimum availability cannot exceed 100"
        )
        BigDecimal minAvailabilityPct,

        @DecimalMin(
                value = "0.00",
                message = "Maximum availability must be at least 0"
        )
        @DecimalMax(
                value = "100.00",
                message = "Maximum availability cannot exceed 100"
        )
        BigDecimal maxAvailabilityPct,

        @NotNull(
                message = "Payout percentage is required"
        )
        @DecimalMin(
                value = "0.01",
                message = "Payout percentage must be positive"
        )
        @DecimalMax(
                value = "100.0",
                message = "Payout percentage cannot exceed 100"
        )
        BigDecimal payoutPercentage

) {
}
