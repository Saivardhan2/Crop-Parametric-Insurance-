package org.example.policyservice.util;

import org.example.policyservice.dto.response
        .InsurancePlanResponse;
import org.example.policyservice.entity.InsurancePlan;

public final class InsurancePlanMapper {

    private InsurancePlanMapper() {
    }

    public static InsurancePlanResponse toResponse(
            InsurancePlan plan) {

        return new InsurancePlanResponse(
                plan.getPlanId(),
                plan.getPlanName(),
                plan.getDescription(),
                plan.getMinimumCoverage(),
                plan.getMaximumCoverage(),
                plan.getBasePremium(),
                plan.getMaximumPolicyDays(),
                plan.getStatus(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}