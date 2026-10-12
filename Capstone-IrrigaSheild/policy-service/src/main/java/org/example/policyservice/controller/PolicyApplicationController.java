package org.example.policyservice.controller;

import org.example.policyservice.dto.request.PolicyApplicationRequest;
import org.example.policyservice.dto.response.PolicyApplicationResponse;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.service.PolicyApplicationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/applications")
@PreAuthorize("hasRole('FARMER')")
public class PolicyApplicationController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PolicyApplicationController.class);

    private final PolicyApplicationService applicationService;

    public PolicyApplicationController(
            PolicyApplicationService applicationService) {

        this.applicationService = applicationService;
    }

    @PostMapping
    public Mono<ResponseEntity<PolicyApplicationResponse>>
    createApplication(
            Authentication authentication,
            @Valid @RequestBody
            PolicyApplicationRequest request) {

        return applicationService
                .createApplication(
                        getUser(authentication),
                        request
                )
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @GetMapping("/my-applications")
    public Flux<PolicyApplicationResponse>
    getMyApplications(
            Authentication authentication) {

        return applicationService
                .getMyApplications(
                        getUser(authentication)
                );
    }

    @GetMapping("/{applicationId}")
    public Mono<ResponseEntity<PolicyApplicationResponse>>
    getMyApplication(
            Authentication authentication,
            @PathVariable Long applicationId) {

        return applicationService
                .getMyApplication(
                        getUser(authentication),
                        applicationId
                )
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{applicationId}")
    public Mono<ResponseEntity<PolicyApplicationResponse>>
    updateApplication(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            PolicyApplicationRequest request) {

        return applicationService
                .updateApplication(
                        getUser(authentication),
                        applicationId,
                        request
                )
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{applicationId}/submit")
    public Mono<ResponseEntity<PolicyApplicationResponse>>
    submitApplication(
            Authentication authentication,
            @PathVariable Long applicationId) {

        return applicationService
                .submitApplication(
                        getUser(authentication),
                        applicationId
                )
                .map(ResponseEntity::ok);
    }

    private AuthenticatedUser getUser(
            Authentication authentication) {

        return (AuthenticatedUser)
                authentication.getPrincipal();
    }
}