package org.example.eventclaimsservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PolicyClaimConfigurationResponse(

        Long policyId,
        Long farmerUserId,
        Long farmId,
        Long cropId,
        Long planId,
        BigDecimal coverageAmount,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        TriggerRuleConfig triggerRule,
        List<PayoutSlabConfig> payoutSlabs

) {
}
