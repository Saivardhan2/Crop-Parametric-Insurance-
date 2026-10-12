package org.example.eventclaimsservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyDetails(

        Long policyId,
        Long farmerUserId,
        Long farmId,
        Long cropId,
        Long planId,
        BigDecimal coverageAmount,
        LocalDate startDate,
        LocalDate endDate,
        String status

) {
}