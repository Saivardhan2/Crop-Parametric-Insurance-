package org.example.policyservice.dto.response;

import java.math.BigDecimal;

public record TriggerRuleConfigResponse(

        Long triggerRuleId,
        Long planId,
        String cropType,
        String growthStage,
        BigDecimal availabilityThresholdPercentage,
        Integer minimumContinuousHours,
        Integer rollingWindowDays,
        Integer requiredShortageDays,
        String status

) {
}
