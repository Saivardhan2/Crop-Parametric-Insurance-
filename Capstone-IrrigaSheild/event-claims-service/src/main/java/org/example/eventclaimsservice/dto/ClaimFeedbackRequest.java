package org.example.eventclaimsservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ClaimFeedbackRequest(
        @NotBlank(message = "Feedback status is required (e.g. SATISFIED or REQUEST_MORE_AMOUNT)")
        String feedbackStatus,

        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer feedbackRating,

        @Size(max = 1000, message = "Comments cannot exceed 1000 characters")
        String feedbackComments,

        @DecimalMin(value = "0.0", message = "Requested additional amount cannot be negative")
        BigDecimal requestedAdditionalAmount
) {
}