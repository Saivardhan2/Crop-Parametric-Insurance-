package org.example.eventclaimsservice.dto;

import jakarta.validation.constraints.Size;

public record ClaimDecisionRequest(
        @Size(max = 500, message = "Remarks cannot exceed 500 characters")
        String remarks
) {
}