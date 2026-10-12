package org.example.policyservice.dto.response;

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
        TriggerRuleConfigResponse triggerRule,
        List<PayoutSlabConfigResponse> payoutSlabs

) {
}
