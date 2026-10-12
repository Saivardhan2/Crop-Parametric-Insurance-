package org.example.eventclaimsservice.dto;

import java.math.BigDecimal;

public record PayoutSlabConfig(

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
