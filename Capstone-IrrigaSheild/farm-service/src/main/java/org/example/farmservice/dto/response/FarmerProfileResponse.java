package org.example.farmservice.dto.response;

import java.time.LocalDateTime;

public record FarmerProfileResponse(

        Long farmerId,
        Long authUserId,
        String email,
        String fullName,
        String phoneNumber,
        String addressLine,
        String village,
        String district,
        String state,
        String postalCode,
        String bankAccountLastFour,
        String verificationStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}