package org.example.farmservice.controller;

import org.example.farmservice.dto.response.FarmRiskProfileResponse;
import org.example.farmservice.service.FarmRiskProfileService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/farms")
public class FarmRiskProfileController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FarmRiskProfileController.class);

    private final FarmRiskProfileService farmRiskProfileService;

    public FarmRiskProfileController(
            FarmRiskProfileService farmRiskProfileService) {

        this.farmRiskProfileService = farmRiskProfileService;
    }

    @GetMapping("/{farmId}/risk-profile")
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'ADMIN')")
    public Mono<ResponseEntity<FarmRiskProfileResponse>> getRiskProfile(
            @PathVariable Long farmId) {

        return farmRiskProfileService
                .getRiskProfile(farmId)
                .map(ResponseEntity::ok);
    }
}