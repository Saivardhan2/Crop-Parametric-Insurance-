package org.example.policyservice.controller;

import org.example.policyservice.dto.request
        .PayoutSlabRequest;
import org.example.policyservice.dto.request
        .TriggerRuleRequest;
import org.example.policyservice.dto.response
        .PayoutSlabResponse;
import org.example.policyservice.dto.response
        .TriggerRuleResponse;
import org.example.policyservice.service
        .PayoutSlabService;
import org.example.policyservice.service
        .TriggerRuleService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost
        .PreAuthorize;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTriggerRuleController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminTriggerRuleController.class);

    private final TriggerRuleService triggerRuleService;
    private final PayoutSlabService payoutSlabService;

    public AdminTriggerRuleController(
            TriggerRuleService triggerRuleService,
            PayoutSlabService payoutSlabService) {

        this.triggerRuleService = triggerRuleService;
        this.payoutSlabService = payoutSlabService;
    }

    @PostMapping("/plans/{planId}/trigger-rules")
    public Mono<ResponseEntity<TriggerRuleResponse>>
    createTriggerRule(
            @PathVariable Long planId,
            @Valid @RequestBody
            TriggerRuleRequest request) {

        return triggerRuleService
                .createRule(planId, request)
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @GetMapping("/plans/{planId}/trigger-rules")
    public Flux<TriggerRuleResponse> getTriggerRules(
            @PathVariable Long planId) {

        return triggerRuleService
                .getRulesForPlan(planId);
    }

    @PostMapping(
            "/trigger-rules/{triggerRuleId}/payout-slabs"
    )
    public Mono<ResponseEntity<PayoutSlabResponse>>
    createPayoutSlab(
            @PathVariable Long triggerRuleId,
            @Valid @RequestBody
            PayoutSlabRequest request) {

        return payoutSlabService
                .createSlab(triggerRuleId, request)
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @GetMapping(
            "/trigger-rules/{triggerRuleId}/payout-slabs"
    )
    public Flux<PayoutSlabResponse> getPayoutSlabs(
            @PathVariable Long triggerRuleId) {

        return payoutSlabService
                .getSlabs(triggerRuleId);
    }
}