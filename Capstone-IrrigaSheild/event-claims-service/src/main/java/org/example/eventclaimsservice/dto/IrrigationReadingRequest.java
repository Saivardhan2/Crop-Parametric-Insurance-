package org.example.eventclaimsservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IrrigationReadingRequest(

        @NotNull(message = "Policy ID is required")
        @Positive(message = "Policy ID must be positive")
        Long policyId,

        @NotNull(message = "Availability percentage is required")
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "100.0")
        BigDecimal availabilityPercentage,

        LocalDateTime recordedAt

) {
}