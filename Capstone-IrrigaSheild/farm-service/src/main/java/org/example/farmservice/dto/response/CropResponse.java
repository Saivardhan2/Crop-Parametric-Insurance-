package org.example.farmservice.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CropResponse(

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