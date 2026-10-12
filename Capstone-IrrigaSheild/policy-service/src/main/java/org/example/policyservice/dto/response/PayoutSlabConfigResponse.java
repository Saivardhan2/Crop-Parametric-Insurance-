package org.example.policyservice.dto.response;

import java.math.BigDecimal;

public record PayoutSlabConfigResponse(

        Long payoutSlabId,
        Long triggerRuleId,
        Integer minimumDurationHours,
        Integer maximumDurationHours,
        BigDecimal minAvailabilityPct,
        BigDecimal maxAvailabilityPct,
        BigDecimal payoutPercentage,
        String status

) {
}
