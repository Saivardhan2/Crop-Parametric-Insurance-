package org.example.policyservice.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InsurancePlanRequest(

        @NotBlank(message = "Plan name is required")
        @Size(min = 3, max = 100)
        String planName,

        @NotBlank(message = "Description is required")
        @Size(max = 500)
        String description,

        @NotNull(message = "Minimum coverage is required")
        @DecimalMin(value = "1000.00")
        BigDecimal minimumCoverage,

        @NotNull(message = "Maximum coverage is required")
        @DecimalMin(value = "1000.00")
        BigDecimal maximumCoverage,

        @NotNull(message = "Base premium is required")
        @DecimalMin(value = "1.00")
        BigDecimal basePremium,

        @NotNull(message = "Maximum policy days is required")
        @Min(value = 1)
        Integer maximumPolicyDays

) {
}