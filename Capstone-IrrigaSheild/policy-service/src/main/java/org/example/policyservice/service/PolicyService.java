package org.example.policyservice.service;

import org.example.policyservice.dto.response.PolicyResponse;
import org.example.policyservice.security.AuthenticatedUser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolicyService {

    Mono<PolicyResponse> getApprovedQuote(
            AuthenticatedUser farmer,
            Long applicationId
    );

    Mono<PolicyResponse> acceptQuote(
            AuthenticatedUser farmer,
            Long applicationId
    );

    Flux<PolicyResponse> getMyPolicies(
            AuthenticatedUser farmer
    );
}