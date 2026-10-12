package org.example.farmservice.util;

import org.example.farmservice.dto.response.FarmResponse;
import org.example.farmservice.entity.Farm;

public final class FarmMapper {

    private FarmMapper() {
    }

    public static FarmResponse toResponse(Farm farm) {

        return new FarmResponse(
                farm.getFarmId(),
                farm.getFarmerId(),
                farm.getFarmName(),
                farm.getSurveyNumber(),
                farm.getAddressLine(),
                farm.getVillage(),
                farm.getDistrict(),
                farm.getState(),
                farm.getPostalCode(),
                farm.getAreaInAcres(),
                farm.getSoilType(),
                farm.getOwnershipType(),
                farm.getLatitude(),
                farm.getLongitude(),
                farm.getStatus(),
                farm.getCreatedAt(),
                farm.getUpdatedAt()
        );
    }
}