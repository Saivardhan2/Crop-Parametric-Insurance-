package org.example.eventclaimsservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ClaimDecisionRequest(
        @Size(max = 500, message = "Remarks cannot exceed 500 characters")
        String remarks,

        @DecimalMin(value = "0.0", inclusive = false, message = "Adjusted compensation amount must be greater than zero")
        BigDecimal adjustedAmount
) {
    public ClaimDecisionRequest(String remarks) {
        this(remarks, null);
    }
}
