package org.example.farmservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FarmResponse(

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