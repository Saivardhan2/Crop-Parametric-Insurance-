package org.example.policyservice.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TriggerRuleRequest(

        @NotBlank(message = "Crop type is required")
        @Size(max = 50)
        String cropType,

        @NotBlank(message = "Growth stage is required")
        @Pattern(
                regexp =
                        "^(SOWING|GERMINATION|VEGETATIVE|FLOWERING|FRUITING|MATURITY)$",
                message = "Invalid crop growth stage"
        )
        String growthStage,

        @NotNull(
                message = "Availability threshold is required"
        )
        @DecimalMin(
                value = "0.0",
                message = "Threshold cannot be below zero"
        )
        @DecimalMax(
                value = "100.0",
                message = "Threshold cannot exceed 100"
        )
        BigDecimal availabilityThresholdPercentage,

        @NotNull(
                message = "Minimum continuous hours is required"
        )
        @Min(
                value = 1,
                message = "Minimum continuous hours must be positive"
        )
        Integer minimumContinuousHours,

        @NotNull(
                message = "Rolling window days is required"
        )
        @Min(
                value = 1,
                message = "Rolling window must contain at least one day"
        )
        Integer rollingWindowDays,

        @NotNull(
                message = "Required shortage days is required"
        )
        @Min(
                value = 1,
                message = "Required shortage must contain at least one day"
        )
        Integer requiredShortageDays

) {
}