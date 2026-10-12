package org.example.farmservice.service;

import org.example.farmservice.dto.response
        .FarmApplicationValidationResponse;
import org.example.farmservice.security.AuthenticatedUser;

import reactor.core.publisher.Mono;

public interface FarmApplicationValidationService {

    Mono<FarmApplicationValidationResponse>
    validateForApplication(
            AuthenticatedUser user,
            Long farmId,
            Long cropId
    );
}