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
        LocalDateTime recordedAt,
        Long claimsOfficerId,
        String claimsOfficerRemarks,
        String feedbackStatus,
        Integer feedbackRating,
        String feedbackComments,
        BigDecimal requestedAdditionalAmount,
        LocalDateTime feedbackSubmittedAt,
        LocalDateTime settledAt

) {
    public ClaimResponse(
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
            LocalDateTime recordedAt) {
        this(
                readingId,
                claimId,
                policyId,
                farmId,
                availabilityPercentage,
                thresholdBreached,
                payoutPercentage,
                compensationAmount,
                payoutStatus,
                claimStatus,
                recordedAt,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}