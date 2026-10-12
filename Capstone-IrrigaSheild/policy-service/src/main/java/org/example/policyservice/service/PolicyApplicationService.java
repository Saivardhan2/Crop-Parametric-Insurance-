package org.example.policyservice.service;

import org.example.policyservice.dto.request.PolicyApplicationRequest;
import org.example.policyservice.dto.response.PolicyApplicationResponse;
import org.example.policyservice.security.AuthenticatedUser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolicyApplicationService {

    Mono<PolicyApplicationResponse> createApplication(
            AuthenticatedUser user,
            PolicyApplicationRequest request
    );

    Flux<PolicyApplicationResponse> getMyApplications(
            AuthenticatedUser user
    );

    Mono<PolicyApplicationResponse> getMyApplication(
            AuthenticatedUser user,
            Long applicationId
    );

    Mono<PolicyApplicationResponse> updateApplication(
            AuthenticatedUser user,
            Long applicationId,
            PolicyApplicationRequest request
    );

    Mono<PolicyApplicationResponse> submitApplication(
            AuthenticatedUser user,
            Long applicationId
    );
}