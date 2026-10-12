package org.example.policyservice.underwriting.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.PolicyApplication;
import org.example.policyservice.underwriting.entity.UnderwritingCase;

public record UnderwritingResponse(
        Long caseId,
        Long applicationId,
        Long farmerUserId,
        String farmerEmail,
        Long farmId,
        Long cropId,
        Long planId,
        String planName,
        BigDecimal requestedCoverage,
        LocalDate proposedStartDate,
        LocalDate proposedEndDate,
        String applicationStatus,
        Long underwriterUserId,
        String underwriterEmail,
        Integer waterAvailabilityScore,
        Integer equipmentConditionScore,
        Integer historicalFailureScore,
        Integer seasonalRiskScore,
        Integer backupSourceScore,
        Integer overallRiskScore,
        String riskLevel,
        BigDecimal basePremium,
        BigDecimal riskLoading,
        BigDecimal recommendedPremium,
        BigDecimal recommendedCoverage,
        String decision,
        String remarks,
        String underwritingStatus,
        LocalDateTime reviewStartedAt,
        LocalDateTime reviewCompletedAt
) {

    public static UnderwritingResponse from(
            PolicyApplication application,
            InsurancePlan plan,
            UnderwritingCase underwritingCase) {

        return new UnderwritingResponse(
                underwritingCase == null ? null : underwritingCase.getCaseId(),
                application.getApplicationId(),
                application.getFarmerUserId(),
                application.getFarmerEmail(),
                application.getFarmId(),
                application.getCropId(),
                plan.getPlanId(),
                plan.getPlanName(),
                application.getRequestedCoverage(),
                application.getProposedStartDate(),
                application.getProposedEndDate(),
                application.getStatus(),
                underwritingCase == null ? null : underwritingCase.getUnderwriterUserId(),
                underwritingCase == null ? null : underwritingCase.getUnderwriterEmail(),
                underwritingCase == null ? null : underwritingCase.getWaterAvailabilityScore(),
                underwritingCase == null ? null : underwritingCase.getEquipmentConditionScore(),
                underwritingCase == null ? null : underwritingCase.getHistoricalFailureScore(),
                underwritingCase == null ? null : underwritingCase.getSeasonalRiskScore(),
                underwritingCase == null ? null : underwritingCase.getBackupSourceScore(),
                underwritingCase == null ? null : underwritingCase.getOverallRiskScore(),
                underwritingCase == null ? null : underwritingCase.getRiskLevel(),
                underwritingCase == null ? null : underwritingCase.getBasePremium(),
                underwritingCase == null ? null : underwritingCase.getRiskLoading(),
                underwritingCase == null ? null : underwritingCase.getRecommendedPremium(),
                underwritingCase == null ? null : underwritingCase.getRecommendedCoverage(),
                underwritingCase == null ? null : underwritingCase.getDecision(),
                underwritingCase == null ? null : underwritingCase.getRemarks(),
                underwritingCase == null ? null : underwritingCase.getStatus(),
                underwritingCase == null ? null : underwritingCase.getReviewStartedAt(),
                underwritingCase == null ? null : underwritingCase.getReviewCompletedAt()
        );
    }
}
