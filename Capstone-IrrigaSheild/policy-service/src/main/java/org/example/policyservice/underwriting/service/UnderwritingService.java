package org.example.policyservice.underwriting.service;

import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.underwriting.dto.request
        .DecisionRequest;
import org.example.policyservice.underwriting.dto.response
        .UnderwritingResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UnderwritingService {

    Flux<UnderwritingResponse> getPendingApplications();

    Flux<UnderwritingResponse> getAllCases();

    Flux<UnderwritingResponse> getMyCases(
            AuthenticatedUser underwriter
    );

    Mono<UnderwritingResponse> startReview(
            AuthenticatedUser underwriter,
            Long applicationId
    );

    Mono<UnderwritingResponse> assess(
            AuthenticatedUser underwriter,
            Long applicationId
    );

    Mono<UnderwritingResponse> approve(
            AuthenticatedUser underwriter,
            Long applicationId,
            DecisionRequest request
    );

    Mono<UnderwritingResponse> reject(
            AuthenticatedUser underwriter,
            Long applicationId,
            DecisionRequest request
    );
}