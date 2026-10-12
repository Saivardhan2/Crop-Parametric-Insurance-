package org.example.farmservice.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CropRequest(

        @NotBlank(message = "Crop type is required")
        @Size(max = 50)
        String cropType,

        @Size(max = 100)
        String variety,

        @NotNull(message = "Sowing date is required")
        @PastOrPresent(
                message = "Sowing date cannot be in the future"
        )
        LocalDate sowingDate,

        @NotNull(message = "Expected harvest date is required")
        @Future(
                message = "Expected harvest date must be in the future"
        )
        LocalDate expectedHarvestDate,

        @NotBlank(message = "Growth stage is required")
        @Pattern(
                regexp = "^(SOWING|GERMINATION|VEGETATIVE|FLOWERING|FRUITING|MATURITY)$",
                message = "Invalid crop growth stage"
        )
        String growthStage,

        @NotBlank(message = "Irrigation requirement is required")
        @Pattern(
                regexp = "^(LOW|MEDIUM|HIGH)$",
                message = "Irrigation requirement must be LOW, MEDIUM, or HIGH"
        )
        String irrigationRequirement

) {
}