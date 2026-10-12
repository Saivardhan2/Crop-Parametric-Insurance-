package org.example.policyservice.controller;

import java.util.List;

import org.example.policyservice.dto.response.PayoutSlabConfigResponse;
import org.example.policyservice.dto.response.PolicyClaimConfigurationResponse;
import org.example.policyservice.dto.response.PolicyInternalResponse;
import org.example.policyservice.dto.response.TriggerRuleConfigResponse;
import org.example.policyservice.exception.ApiException;
import org.example.policyservice.repository.PayoutSlabRepository;
import org.example.policyservice.repository.PolicyRepository;
import org.example.policyservice.repository.TriggerRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/internal/policies")
@PreAuthorize(
        "hasAnyRole('DATA_PROVIDER', 'CLAIMS_OFFICER', 'ADMIN')"
)
public class InternalPolicyController {

    private static final Logger log = LoggerFactory.getLogger(InternalPolicyController.class);

    private final PolicyRepository policyRepository;
    private final TriggerRuleRepository triggerRuleRepository;
    private final PayoutSlabRepository payoutSlabRepository;

    public InternalPolicyController(
            PolicyRepository policyRepository,
            TriggerRuleRepository triggerRuleRepository,
            PayoutSlabRepository payoutSlabRepository) {

        this.policyRepository = policyRepository;
        this.triggerRuleRepository = triggerRuleRepository;
        this.payoutSlabRepository = payoutSlabRepository;
    }

    @GetMapping("/{policyId}")
    public Mono<PolicyInternalResponse> getActivePolicy(
            @PathVariable Long policyId) {

        log.info("Fetching active internal policy configuration for policyId={}", policyId);

        return policyRepository
                .findByPolicyIdAndStatus(
                        policyId,
                        "ACTIVE"
                )
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "ACTIVE_POLICY_NOT_FOUND",
                                        "Active policy was not found"
                                )
                        )
                )
                .map(policy ->
                        new PolicyInternalResponse(
                                policy.getPolicyId(),
                                policy.getFarmerUserId(),
                                policy.getFarmId(),
                                policy.getCropId(),
                                policy.getPlanId(),
                                policy.getCoverageAmount(),
                                policy.getStartDate(),
                                policy.getEndDate(),
                                policy.getStatus()
                        )
                );
    }

    @GetMapping("/{policyId}/claim-configuration")
    public Mono<PolicyClaimConfigurationResponse> getPolicyClaimConfiguration(
            @PathVariable Long policyId) {

        log.info("Resolving claim trigger rule and payout slabs for policyId={}", policyId);

        return policyRepository
                .findByPolicyIdAndStatus(
                        policyId,
                        "ACTIVE"
                )
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "ACTIVE_POLICY_NOT_FOUND",
                                        "Active policy was not found"
                                )
                        )
                )
                .flatMap(policy ->
                        triggerRuleRepository
                                .findAllByPlanIdAndStatusOrderByCreatedAtDesc(
                                        policy.getPlanId(),
                                        "ACTIVE"
                                )
                                .next()
                                .flatMap(rule ->
                                        payoutSlabRepository
                                                .findAllByTriggerRuleIdAndStatus(
                                                        rule.getTriggerRuleId(),
                                                        "ACTIVE"
                                                )
                                                .map(slab -> new PayoutSlabConfigResponse(
                                                        slab.getPayoutSlabId(),
                                                        slab.getTriggerRuleId(),
                                                        slab.getMinimumDurationHours(),
                                                        slab.getMaximumDurationHours(),
                                                        slab.getMinAvailabilityPct(),
                                                        slab.getMaxAvailabilityPct(),
                                                        slab.getPayoutPercentage(),
                                                        slab.getStatus()
                                                ))
                                                .collectList()
                                                .map(slabs -> {
                                                    TriggerRuleConfigResponse ruleConfig =
                                                            new TriggerRuleConfigResponse(
                                                                    rule.getTriggerRuleId(),
                                                                    rule.getPlanId(),
                                                                    rule.getCropType(),
                                                                    rule.getGrowthStage(),
                                                                    rule.getAvailabilityThresholdPercentage(),
                                                                    rule.getMinimumContinuousHours(),
                                                                    rule.getRollingWindowDays(),
                                                                    rule.getRequiredShortageDays(),
                                                                    rule.getStatus()
                                                            );

                                                    return new PolicyClaimConfigurationResponse(
                                                            policy.getPolicyId(),
                                                            policy.getFarmerUserId(),
                                                            policy.getFarmId(),
                                                            policy.getCropId(),
                                                            policy.getPlanId(),
                                                            policy.getCoverageAmount(),
                                                            policy.getStartDate(),
                                                            policy.getEndDate(),
                                                            policy.getStatus(),
                                                            ruleConfig,
                                                            slabs
                                                    );
                                                })
                                )
                                .defaultIfEmpty(
                                        new PolicyClaimConfigurationResponse(
                                                policy.getPolicyId(),
                                                policy.getFarmerUserId(),
                                                policy.getFarmId(),
                                                policy.getCropId(),
                                                policy.getPlanId(),
                                                policy.getCoverageAmount(),
                                                policy.getStartDate(),
                                                policy.getEndDate(),
                                                policy.getStatus(),
                                                null,
                                                List.of()
                                        )
                                )
                );
    }
}
