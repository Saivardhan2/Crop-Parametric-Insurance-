package org.example.policyservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TriggerRuleResponse(

        Long triggerRuleId,
        Long planId,
        String cropType,
        String growthStage,
        BigDecimal availabilityThresholdPercentage,
        Integer minimumContinuousHours,
        Integer rollingWindowDays,
        Integer requiredShortageDays,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
