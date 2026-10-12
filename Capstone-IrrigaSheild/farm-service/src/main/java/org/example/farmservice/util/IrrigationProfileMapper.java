package org.example.farmservice.util;

import org.example.farmservice.dto.response
        .IrrigationProfileResponse;
import org.example.farmservice.entity.IrrigationProfile;

public final class IrrigationProfileMapper {

    private IrrigationProfileMapper() {
    }

    public static IrrigationProfileResponse toResponse(
            IrrigationProfile profile) {

        return new IrrigationProfileResponse(
                profile.getIrrigationId(),
                profile.getFarmId(),
                profile.getWaterSource(),
                profile.getIrrigationMethod(),
                profile.getEquipmentCondition(),
                profile.getBackupWaterSource(),
                profile.getHistoricalFailureCount(),
                profile.getAverageWaterAvailabilityPercentage(),
                profile.getSeasonalRisk(),
                profile.getMaintenanceFrequency(),
                profile.getLastMaintenanceDate(),
                profile.getStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}