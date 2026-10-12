package org.example.policyservice.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PolicyApplicationRequest(

        @NotNull(message = "Farm ID is required")
        @Positive(message = "Farm ID must be positive")
        Long farmId,

        @NotNull(message = "Crop ID is required")
        @Positive(message = "Crop ID must be positive")
        Long cropId,

        @NotNull(message = "Plan ID is required")
        @Positive(message = "Plan ID must be positive")
        Long planId,

        @NotNull(message = "Requested coverage is required")
        @DecimalMin(
                value = "1000.00",
                message = "Requested coverage must be at least 1000"
        )
        BigDecimal requestedCoverage,

        @NotNull(message = "Proposed start date is required")
        @FutureOrPresent(
                message = "Proposed start date cannot be in the past"
        )
        LocalDate proposedStartDate,

        @NotNull(message = "Proposed end date is required")
        @Future(
                message = "Proposed end date must be in the future"
        )
        LocalDate proposedEndDate

) {
}