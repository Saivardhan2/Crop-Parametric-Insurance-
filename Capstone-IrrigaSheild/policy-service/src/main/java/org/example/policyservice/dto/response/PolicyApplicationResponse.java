package org.example.policyservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PolicyApplicationResponse(

        Long applicationId,
        Long farmerUserId,
        String farmerEmail,
        Long farmId,
        Long cropId,
        Long planId,
        String planName,
        BigDecimal requestedCoverage,
        LocalDate proposedStartDate,
        LocalDate proposedEndDate,
        String status,
        LocalDateTime submittedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}