package org.example.policyservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayoutSlabResponse(

        Long payoutSlabId,
        Long triggerRuleId,
        Integer minimumDurationHours,
        Integer maximumDurationHours,
        BigDecimal minAvailabilityPct,
        BigDecimal maxAvailabilityPct,
        BigDecimal payoutPercentage,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
