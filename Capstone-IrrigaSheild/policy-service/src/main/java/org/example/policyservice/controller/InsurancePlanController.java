package org.example.policyservice.controller;

import org.example.policyservice.dto.response
        .InsurancePlanResponse;
import org.example.policyservice.service
        .InsurancePlanService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/plans")
public class InsurancePlanController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InsurancePlanController.class);

    private final InsurancePlanService planService;

    public InsurancePlanController(
            InsurancePlanService planService) {

        this.planService = planService;
    }

    @GetMapping("/active")
    @PreAuthorize(
            "hasAnyRole('FARMER', 'UNDERWRITER', 'ADMIN')"
    )
    public Flux<InsurancePlanResponse> getActivePlans() {
        return planService.getActivePlans();
    }

    @GetMapping("/{planId}")
    @PreAuthorize(
            "hasAnyRole('FARMER', 'UNDERWRITER', 'ADMIN')"
    )
    public Mono<ResponseEntity<InsurancePlanResponse>>
    getPlan(@PathVariable Long planId) {

        return planService.getPlan(planId)
                .map(ResponseEntity::ok);
    }
}