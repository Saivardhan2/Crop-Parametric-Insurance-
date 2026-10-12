package org.example.policyservice.underwriting.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DecisionRequest(

        @NotBlank(message = "Remarks are required")
        @Size(
                min = 5,
                max = 500,
                message = "Remarks must contain between 5 and 500 characters"
        )
        String remarks

) {
}
