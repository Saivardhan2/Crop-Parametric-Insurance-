package org.example.policyservice.underwriting.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FarmRiskProfileResponse(

        FarmDetails farm,
        List<CropDetails> crops,
        IrrigationDetails irrigationProfile

) {

    public record FarmDetails(
            Long farmId,
            Long farmerId,
            String farmName,
            String surveyNumber,
            String addressLine,
            String village,
            String district,
            String state,
            String postalCode,
            BigDecimal areaInAcres,
            String soilType,
            String ownershipType,
            BigDecimal latitude,
            BigDecimal longitude,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record CropDetails(
            Long cropId,
            Long farmId,
            String cropType,
            String variety,
            LocalDate sowingDate,
            LocalDate expectedHarvestDate,
            String growthStage,
            String irrigationRequirement,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record IrrigationDetails(
            Long irrigationId,
            Long farmId,
            String waterSource,
            String irrigationMethod,
            String equipmentCondition,
            String backupWaterSource,
            Integer historicalFailureCount,
            BigDecimal averageWaterAvailabilityPercentage,
            String seasonalRisk,
            String maintenanceFrequency,
            LocalDate lastMaintenanceDate,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }
}