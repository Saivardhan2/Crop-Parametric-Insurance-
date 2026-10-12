package org.example.farmservice.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FarmRequest(

        @NotBlank(message = "Farm name is required")
        @Size(
                min = 2,
                max = 100,
                message = "Farm name must contain between 2 and 100 characters"
        )
        String farmName,

        @Size(
                max = 50,
                message = "Survey number cannot exceed 50 characters"
        )
        String surveyNumber,

        @NotBlank(message = "Farm address is required")
        @Size(
                max = 200,
                message = "Farm address cannot exceed 200 characters"
        )
        String addressLine,

        @NotBlank(message = "Village is required")
        @Size(max = 100)
        String village,

        @NotBlank(message = "District is required")
        @Size(max = 100)
        String district,

        @NotBlank(message = "State is required")
        @Size(max = 100)
        String state,

        @NotBlank(message = "Postal code is required")
        @Pattern(
                regexp = "^[1-9][0-9]{5}$",
                message = "Postal code must be a valid 6-digit PIN code"
        )
        String postalCode,

        @NotNull(message = "Farm area is required")
        @DecimalMin(
                value = "0.01",
                message = "Farm area must be greater than zero"
        )
        @Digits(
                integer = 8,
                fraction = 2,
                message = "Farm area can have at most two decimal places"
        )
        BigDecimal areaInAcres,

        @NotBlank(message = "Soil type is required")
        @Size(max = 50)
        String soilType,

        @NotBlank(message = "Ownership type is required")
        @Pattern(
                regexp = "^(OWNED|LEASED|SHARED)$",
                message = "Ownership type must be OWNED, LEASED, or SHARED"
        )
        String ownershipType,

        @DecimalMin(
                value = "-90.0",
                message = "Latitude cannot be below -90"
        )
        @DecimalMax(
                value = "90.0",
                message = "Latitude cannot exceed 90"
        )
        BigDecimal latitude,

        @DecimalMin(
                value = "-180.0",
                message = "Longitude cannot be below -180"
        )
        @DecimalMax(
                value = "180.0",
                message = "Longitude cannot exceed 180"
        )
        BigDecimal longitude

) {
}