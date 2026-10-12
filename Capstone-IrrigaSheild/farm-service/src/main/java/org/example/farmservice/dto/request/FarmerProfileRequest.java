package org.example.farmservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FarmerProfileRequest(

        @NotBlank(message = "Full name is required")
        @Size(
                min = 2,
                max = 100,
                message = "Full name must contain between 2 and 100 characters"
        )
        String fullName,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[6-9][0-9]{9}$",
                message = "Phone number must be a valid 10-digit Indian mobile number"
        )
        String phoneNumber,

        @NotBlank(message = "Address is required")
        @Size(
                max = 200,
                message = "Address cannot exceed 200 characters"
        )
        String addressLine,

        @NotBlank(message = "Village is required")
        @Size(
                max = 100,
                message = "Village cannot exceed 100 characters"
        )
        String village,

        @NotBlank(message = "District is required")
        @Size(
                max = 100,
                message = "District cannot exceed 100 characters"
        )
        String district,

        @NotBlank(message = "State is required")
        @Size(
                max = 100,
                message = "State cannot exceed 100 characters"
        )
        String state,

        @NotBlank(message = "Postal code is required")
        @Pattern(
                regexp = "^[1-9][0-9]{5}$",
                message = "Postal code must be a valid 6-digit PIN code"
        )
        String postalCode,

        @Pattern(
                regexp = "^$|^[0-9]{4}$",
                message = "Bank-account reference must contain the last four digits"
        )
        String bankAccountLastFour

) {
}