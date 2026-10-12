package org.example.policyservice.controller;

import org.example.policyservice.dto.response.PolicyResponse;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.service.PolicyService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/policies")
@PreAuthorize("hasRole('FARMER')")
public class PolicyController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PolicyController.class);

    private final PolicyService policyService;

    public PolicyController(
            PolicyService policyService) {

        this.policyService = policyService;
    }

    @GetMapping(
            "/approved-quote/{applicationId}"
    )
    public Mono<ResponseEntity<PolicyResponse>>
    getApprovedQuote(
            Authentication authentication,
            @PathVariable Long applicationId) {

        return policyService
                .getApprovedQuote(
                        getUser(authentication),
                        applicationId
                )
                .map(ResponseEntity::ok);
    }

    @PostMapping(
            "/approved-quote/{applicationId}/accept"
    )
    public Mono<ResponseEntity<PolicyResponse>>
    acceptQuote(
            Authentication authentication,
            @PathVariable Long applicationId) {

        return policyService
                .acceptQuote(
                        getUser(authentication),
                        applicationId
                )
                .map(ResponseEntity::ok);
    }

    @GetMapping("/my-policies")
    public Flux<PolicyResponse> getMyPolicies(
            Authentication authentication) {

        return policyService
                .getMyPolicies(
                        getUser(authentication)
                );
    }

    private AuthenticatedUser getUser(
            Authentication authentication) {

        return (AuthenticatedUser)
                authentication.getPrincipal();
    }
}