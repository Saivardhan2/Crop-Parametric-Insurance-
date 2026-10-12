package org.example.eventclaimsservice.dto;

import java.math.BigDecimal;

public record TriggerRuleConfig(

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
