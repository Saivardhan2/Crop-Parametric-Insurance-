package org.example.farmservice.util;

import org.example.farmservice.dto.response
        .FarmerProfileResponse;
import org.example.farmservice.entity.FarmerProfile;

public final class FarmerProfileMapper {

    private FarmerProfileMapper() {
    }

    public static FarmerProfileResponse toResponse(
            FarmerProfile profile) {

        return new FarmerProfileResponse(
                profile.getFarmerId(),
                profile.getAuthUserId(),
                profile.getEmail(),
                profile.getFullName(),
                profile.getPhoneNumber(),
                profile.getAddressLine(),
                profile.getVillage(),
                profile.getDistrict(),
                profile.getState(),
                profile.getPostalCode(),
                profile.getBankAccountLastFour(),
                profile.getVerificationStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}