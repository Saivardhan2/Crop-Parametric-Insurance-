package org.example.farmservice.service;

import org.example.farmservice.dto.response
        .FarmRiskProfileResponse;

import reactor.core.publisher.Mono;

public interface FarmRiskProfileService {

    Mono<FarmRiskProfileResponse> getRiskProfile(
            Long farmId
    );
}