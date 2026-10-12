package org.example.farmservice.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

public record IrrigationProfileRequest(

        @NotBlank(message = "Water source is required")
        @Pattern(
                regexp = "^(BOREWELL|CANAL|WELL|RIVER|RESERVOIR|TANKER|RAINWATER)$",
                message = "Invalid water source"
        )
        String waterSource,

        @NotBlank(message = "Irrigation method is required")
        @Pattern(
                regexp = "^(DRIP|SPRINKLER|FLOOD|FURROW|MANUAL)$",
                message = "Invalid irrigation method"
        )
        String irrigationMethod,

        @NotBlank(message = "Equipment condition is required")
        @Pattern(
                regexp = "^(GOOD|FAIR|POOR)$",
                message = "Equipment condition must be GOOD, FAIR, or POOR"
        )
        String equipmentCondition,

        @Pattern(
                regexp = "^$|^(BOREWELL|CANAL|WELL|RIVER|RESERVOIR|TANKER|RAINWATER|NONE)$",
                message = "Invalid backup water source"
        )
        String backupWaterSource,

        @NotNull(message = "Historical failure count is required")
        @Min(
                value = 0,
                message = "Historical failure count cannot be negative"
        )
        Integer historicalFailureCount,

        @NotNull(message = "Average water availability is required")
        @DecimalMin(
                value = "0.0",
                message = "Water availability cannot be below zero"
        )
        @DecimalMax(
                value = "100.0",
                message = "Water availability cannot exceed 100"
        )
        BigDecimal averageWaterAvailabilityPercentage,

        @NotBlank(message = "Seasonal risk is required")
        @Pattern(
                regexp = "^(LOW|MEDIUM|HIGH|VERY_HIGH)$",
                message = "Invalid seasonal risk"
        )
        String seasonalRisk,

        @NotBlank(message = "Maintenance frequency is required")
        @Pattern(
                regexp = "^(MONTHLY|QUARTERLY|HALF_YEARLY|YEARLY|NONE)$",
                message = "Invalid maintenance frequency"
        )
        String maintenanceFrequency,

        @PastOrPresent(
                message = "Last maintenance date cannot be in the future"
        )
        LocalDate lastMaintenanceDate

) {
}