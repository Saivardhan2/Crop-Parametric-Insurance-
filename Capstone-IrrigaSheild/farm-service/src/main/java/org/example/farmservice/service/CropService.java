package org.example.farmservice.service;

import org.example.farmservice.dto.request.CropRequest;
import org.example.farmservice.dto.response.CropResponse;
import org.example.farmservice.security.AuthenticatedUser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CropService {

    Mono<CropResponse> createCrop(
            AuthenticatedUser user,
            Long farmId,
            CropRequest request
    );

    Flux<CropResponse> getCrops(
            AuthenticatedUser user,
            Long farmId
    );

    Mono<CropResponse> getCrop(
            AuthenticatedUser user,
            Long farmId,
            Long cropId
    );

    Mono<CropResponse> updateCrop(
            AuthenticatedUser user,
            Long farmId,
            Long cropId,
            CropRequest request
    );
}