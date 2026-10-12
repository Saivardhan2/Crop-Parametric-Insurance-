package org.example.farmservice.dto.response;

public record FarmApplicationValidationResponse(

        Long farmId,
        Long cropId,
        Long farmerId,
        String farmName,
        String cropType,
        String growthStage,
        boolean irrigationProfileAvailable,
        boolean eligible

) {
}
