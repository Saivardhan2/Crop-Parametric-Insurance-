package org.example.farmservice.service;

import org.example.farmservice.dto.request.FarmRequest;
import org.example.farmservice.dto.response.FarmResponse;
import org.example.farmservice.security.AuthenticatedUser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FarmService {

    Mono<FarmResponse> createFarm(
            AuthenticatedUser authenticatedUser,
            FarmRequest request
    );

    Flux<FarmResponse> getMyFarms(
            AuthenticatedUser authenticatedUser
    );

    Mono<FarmResponse> getMyFarmById(
            AuthenticatedUser authenticatedUser,
            Long farmId
    );

    Mono<FarmResponse> updateMyFarm(
            AuthenticatedUser authenticatedUser,
            Long farmId,
            FarmRequest request
    );
}