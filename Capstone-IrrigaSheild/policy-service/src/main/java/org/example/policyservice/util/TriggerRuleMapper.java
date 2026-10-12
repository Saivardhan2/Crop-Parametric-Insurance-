package org.example.policyservice.util;

import org.example.policyservice.dto.response
        .TriggerRuleResponse;
import org.example.policyservice.entity.TriggerRule;

public final class TriggerRuleMapper {

    private TriggerRuleMapper() {
    }

    public static TriggerRuleResponse toResponse(
            TriggerRule rule) {

        return new TriggerRuleResponse(
                rule.getTriggerRuleId(),
                rule.getPlanId(),
                rule.getCropType(),
                rule.getGrowthStage(),
                rule.getAvailabilityThresholdPercentage(),
                rule.getMinimumContinuousHours(),
                rule.getRollingWindowDays(),
                rule.getRequiredShortageDays(),
                rule.getStatus(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}