package org.example.farmservice.util;

import org.example.farmservice.dto.response.CropResponse;
import org.example.farmservice.entity.Crop;

public final class CropMapper {

    private CropMapper() {
    }

    public static CropResponse toResponse(Crop crop) {

        return new CropResponse(
                crop.getCropId(),
                crop.getFarmId(),
                crop.getCropType(),
                crop.getVariety(),
                crop.getSowingDate(),
                crop.getExpectedHarvestDate(),
                crop.getGrowthStage(),
                crop.getIrrigationRequirement(),
                crop.getStatus(),
                crop.getCreatedAt(),
                crop.getUpdatedAt()
        );
    }
}