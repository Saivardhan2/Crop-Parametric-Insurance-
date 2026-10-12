package org.example.policyservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InsurancePlanResponse(

        Long planId,
        String planName,
        String description,
        BigDecimal minimumCoverage,
        BigDecimal maximumCoverage,
        BigDecimal basePremium,
        Integer maximumPolicyDays,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}