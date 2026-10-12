package org.example.farmservice.service;

import org.example.farmservice.dto.request
        .FarmerProfileRequest;
import org.example.farmservice.dto.response
        .FarmerProfileResponse;
import org.example.farmservice.security.AuthenticatedUser;

import reactor.core.publisher.Mono;

public interface FarmerProfileService {

    Mono<FarmerProfileResponse> createProfile(
            AuthenticatedUser authenticatedUser,
            FarmerProfileRequest request
    );

    Mono<FarmerProfileResponse> getMyProfile(
            AuthenticatedUser authenticatedUser
    );

    Mono<FarmerProfileResponse> updateMyProfile(
            AuthenticatedUser authenticatedUser,
            FarmerProfileRequest request
    );
}