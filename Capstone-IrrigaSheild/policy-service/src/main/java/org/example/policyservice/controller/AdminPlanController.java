package org.example.policyservice.controller;

import org.example.policyservice.dto.request.InsurancePlanRequest;
import org.example.policyservice.dto.response.InsurancePlanResponse;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.service.InsurancePlanService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/plans")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlanController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminPlanController.class);

    private final InsurancePlanService planService;

    public AdminPlanController(
            InsurancePlanService planService) {

        this.planService = planService;
    }

    @PostMapping
    public Mono<ResponseEntity<InsurancePlanResponse>> createPlan(
            Authentication authentication,
            @Valid @RequestBody InsurancePlanRequest request) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        return planService.createPlan(user, request)
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @GetMapping
    public Flux<InsurancePlanResponse> getAllPlans() {
        return planService.getAllPlans();
    }
}