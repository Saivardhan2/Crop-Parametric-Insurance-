package org.example.eventclaimsservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClaimResponse(

        Long readingId,
        Long claimId,
        Long policyId,
        Long farmId,
        BigDecimal availabilityPercentage,
        boolean thresholdBreached,
        BigDecimal payoutPercentage,
        BigDecimal compensationAmount,
        String payoutStatus,
        String claimStatus,
        LocalDateTime recordedAt

) {
}