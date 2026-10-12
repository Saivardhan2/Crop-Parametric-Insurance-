package org.example.farmservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record IrrigationProfileResponse(

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