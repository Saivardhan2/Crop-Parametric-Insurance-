package org.example.policyservice.service;

import org.example.policyservice.dto.request
        .InsurancePlanRequest;
import org.example.policyservice.dto.response
        .InsurancePlanResponse;
import org.example.policyservice.security.AuthenticatedUser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InsurancePlanService {

    Mono<InsurancePlanResponse> createPlan(
            AuthenticatedUser user,
            InsurancePlanRequest request
    );

    Flux<InsurancePlanResponse> getActivePlans();

    Flux<InsurancePlanResponse> getAllPlans();

    Mono<InsurancePlanResponse> getPlan(Long planId);
}