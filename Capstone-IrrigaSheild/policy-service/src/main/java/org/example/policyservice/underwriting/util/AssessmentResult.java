package org.example.policyservice.underwriting.util;

import java.math.BigDecimal;

public record AssessmentResult(

        int waterAvailabilityScore,
        int equipmentConditionScore,
        int historicalFailureScore,
        int seasonalRiskScore,
        int backupSourceScore,

        int overallRiskScore,
        String riskLevel,

        BigDecimal basePremium,
        BigDecimal riskLoading,
        BigDecimal recommendedPremium,
        BigDecimal recommendedCoverage

) {
}