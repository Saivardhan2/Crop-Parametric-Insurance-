package org.example.policyservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PolicyResponse(

        Long policyId,
        Long applicationId,
        Long farmerUserId,
        String farmerEmail,
        Long farmId,
        Long cropId,
        Long planId,
        String planName,
        BigDecimal coverageAmount,
        BigDecimal premiumAmount,
        LocalDate startDate,
        LocalDate endDate,
        String paymentStatus,
        String status,
        LocalDateTime acceptedAt

) {
}