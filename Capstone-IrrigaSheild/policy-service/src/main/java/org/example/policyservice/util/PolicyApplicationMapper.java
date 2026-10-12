package org.example.policyservice.util;

import org.example.policyservice.dto.response.PolicyApplicationResponse;
import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.PolicyApplication;

public final class PolicyApplicationMapper {

    private PolicyApplicationMapper() {
    }

    public static PolicyApplicationResponse toResponse(
            PolicyApplication application,
            InsurancePlan plan) {

        return new PolicyApplicationResponse(
                application.getApplicationId(),
                application.getFarmerUserId(),
                application.getFarmerEmail(),
                application.getFarmId(),
                application.getCropId(),
                application.getPlanId(),
                plan.getPlanName(),
                application.getRequestedCoverage(),
                application.getProposedStartDate(),
                application.getProposedEndDate(),
                application.getStatus(),
                application.getSubmittedAt(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}