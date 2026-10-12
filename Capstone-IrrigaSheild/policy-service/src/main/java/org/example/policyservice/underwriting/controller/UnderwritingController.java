package org.example.policyservice.underwriting.controller;

import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.underwriting.dto.request
        .DecisionRequest;
import org.example.policyservice.underwriting.dto.response
        .UnderwritingResponse;
import org.example.policyservice.underwriting.service
        .UnderwritingService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/underwriting")
@PreAuthorize(
        "hasAnyRole('UNDERWRITER', 'ADMIN')"
)
public class UnderwritingController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UnderwritingController.class);

    private final UnderwritingService underwritingService;

    public UnderwritingController(
            UnderwritingService underwritingService) {

        this.underwritingService = underwritingService;
    }

    @GetMapping("/applications/pending")
    public Flux<UnderwritingResponse>
    getPendingApplications() {

        return underwritingService
                .getPendingApplications();
    }

    @GetMapping("/all-cases")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<UnderwritingResponse> getAllCases() {
        return underwritingService.getAllCases();
    }

    @GetMapping("/my-cases")
    public Flux<UnderwritingResponse> getMyCases(
            Authentication authentication) {

        return underwritingService
                .getMyCases(getUser(authentication));
    }

    @PostMapping(
            "/applications/{applicationId}/start-review"
    )
    public Mono<ResponseEntity<UnderwritingResponse>>
    startReview(
            Authentication authentication,
            @PathVariable Long applicationId) {

        return underwritingService
                .startReview(
                        getUser(authentication),
                        applicationId
                )
                .map(ResponseEntity::ok);
    }

    @PostMapping(
            "/applications/{applicationId}/assess"
    )
    public Mono<ResponseEntity<UnderwritingResponse>>
    assess(
            Authentication authentication,
            @PathVariable Long applicationId) {

        return underwritingService
                .assess(
                        getUser(authentication),
                        applicationId
                )
                .map(ResponseEntity::ok);
    }

    @PutMapping(
            "/applications/{applicationId}/approve"
    )
    public Mono<ResponseEntity<UnderwritingResponse>>
    approve(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody DecisionRequest request) {

        return underwritingService
                .approve(
                        getUser(authentication),
                        applicationId,
                        request
                )
                .map(ResponseEntity::ok);
    }

    @PutMapping(
            "/applications/{applicationId}/reject"
    )
    public Mono<ResponseEntity<UnderwritingResponse>>
    reject(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody DecisionRequest request) {

        return underwritingService
                .reject(
                        getUser(authentication),
                        applicationId,
                        request
                )
                .map(ResponseEntity::ok);
    }

    private AuthenticatedUser getUser(
            Authentication authentication) {

        return (AuthenticatedUser)
                authentication.getPrincipal();
    }
}