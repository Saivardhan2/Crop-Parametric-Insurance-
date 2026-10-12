package org.example.policyservice.underwriting.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.PolicyApplication;
import org.example.policyservice.exception.ApiException;
import org.example.policyservice.underwriting.dto.response
        .FarmRiskProfileResponse;
import org.example.policyservice.underwriting.dto.response
        .FarmRiskProfileResponse.IrrigationDetails;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RiskCalculator {

    public AssessmentResult assess(
            InsurancePlan plan,
            PolicyApplication application,
            FarmRiskProfileResponse riskProfile) {

        IrrigationDetails irrigation =
                riskProfile.irrigationProfile();


        if (irrigation == null) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "IRRIGATION_PROFILE_REQUIRED",
                    "Irrigation profile is required for assessment"
            );
        }

        boolean selectedCropExists =
                riskProfile.crops()
                        .stream()
                        .anyMatch(crop ->
                                crop.cropId().equals(
                                        application.getCropId()
                                )
                        );

        if (!selectedCropExists) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "CROP_NOT_FOUND",
                    "Selected crop was not found"
            );
        }

        int availabilityScore =
                calculateAvailabilityScore(
                        irrigation
                                .averageWaterAvailabilityPercentage()
                );

        int equipmentScore =
                calculateEquipmentScore(
                        irrigation.equipmentCondition()
                );

        int failureScore =
                calculateFailureScore(
                        irrigation.historicalFailureCount()
                );

        int seasonalScore =
                calculateSeasonalScore(
                        irrigation.seasonalRisk()
                );

        int backupScore =
                calculateBackupScore(
                        irrigation.backupWaterSource()
                );

        int totalScore =
                availabilityScore
                        + equipmentScore
                        + failureScore
                        + seasonalScore
                        + backupScore;

        String riskLevel =
                classifyRisk(totalScore);

        BigDecimal basePremium =
                plan.getBasePremium()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal loadingPercentage =
                riskLoadingPercentage(riskLevel);

        BigDecimal riskLoading =
                basePremium
                        .multiply(loadingPercentage)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal recommendedPremium =
                basePremium
                        .add(riskLoading)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal recommendedCoverage =
                application
                        .getRequestedCoverage()
                        .min(plan.getMaximumCoverage())
                        .max(plan.getMinimumCoverage())
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return new AssessmentResult(
                availabilityScore,
                equipmentScore,
                failureScore,
                seasonalScore,
                backupScore,
                totalScore,
                riskLevel,
                basePremium,
                riskLoading,
                recommendedPremium,
                recommendedCoverage
        );
    }

    private int calculateAvailabilityScore(
            BigDecimal availability) {

        if (availability == null) {
            return 30;
        }

        double percentage = availability.doubleValue();

        if (percentage >= 85) {
            return 0;
        }

        if (percentage >= 70) {
            return 10;
        }

        if (percentage >= 50) {
            return 20;
        }

        return 30;
    }

    private int calculateEquipmentScore(
            String equipmentCondition) {

        return switch (normalize(equipmentCondition)) {
            case "GOOD" -> 0;
            case "FAIR" -> 10;
            case "POOR" -> 20;
            default -> 10;
        };
    }

    private int calculateFailureScore(
            Integer failureCount) {

        int failures =
                failureCount == null ? 0 : failureCount;

        if (failures == 0) {
            return 0;
        }

        if (failures <= 2) {
            return 10;
        }

        return 20;
    }

    private int calculateSeasonalScore(
            String seasonalRisk) {

        return switch (normalize(seasonalRisk)) {
            case "LOW" -> 0;
            case "MEDIUM" -> 10;
            case "HIGH" -> 15;
            case "VERY_HIGH" -> 20;
            default -> 10;
        };
    }

    private int calculateBackupScore(
            String backupSource) {

        String value = normalize(backupSource);

        if (value.isBlank() || "NONE".equals(value)) {
            return 10;
        }

        return 0;
    }

    private String classifyRisk(int score) {

        if (score <= 25) {
            return "LOW";
        }

        if (score <= 50) {
            return "MODERATE";
        }

        if (score <= 75) {
            return "HIGH";
        }

        return "VERY_HIGH";
    }

    private BigDecimal riskLoadingPercentage(
            String riskLevel) {

        return switch (riskLevel) {
            case "LOW" -> new BigDecimal("0.05");
            case "MODERATE" -> new BigDecimal("0.15");
            case "HIGH" -> new BigDecimal("0.30");
            case "VERY_HIGH" -> new BigDecimal("0.50");
            default -> new BigDecimal("0.20");
        };
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}