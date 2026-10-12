package org.example.farmservice.service;

import org.example.farmservice.dto.request
        .IrrigationProfileRequest;
import org.example.farmservice.dto.response
        .IrrigationProfileResponse;
import org.example.farmservice.security.AuthenticatedUser;

import reactor.core.publisher.Mono;

public interface IrrigationProfileService {

    Mono<IrrigationProfileResponse> createProfile(
            AuthenticatedUser user,
            Long farmId,
            IrrigationProfileRequest request
    );

    Mono<IrrigationProfileResponse> getProfile(
            AuthenticatedUser user,
            Long farmId
    );

    Mono<IrrigationProfileResponse> updateProfile(
            AuthenticatedUser user,
            Long farmId,
            IrrigationProfileRequest request
    );
}