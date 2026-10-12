package org.example.policyservice.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.policyservice.controller.AdminTriggerRuleController;
import org.example.policyservice.dto.request.PayoutSlabRequest;
import org.example.policyservice.dto.request.TriggerRuleRequest;
import org.example.policyservice.dto.response.PayoutSlabResponse;
import org.example.policyservice.dto.response.TriggerRuleResponse;
import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.PayoutSlab;
import org.example.policyservice.entity.TriggerRule;
import org.example.policyservice.repository.InsurancePlanRepository;
import org.example.policyservice.repository.PayoutSlabRepository;
import org.example.policyservice.repository.TriggerRuleRepository;
import org.example.policyservice.service.PayoutSlabService;
import org.example.policyservice.service.PayoutSlabServiceImpl;
import org.example.policyservice.service.TriggerRuleService;
import org.example.policyservice.service.TriggerRuleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TriggerRuleAndPayoutSlabTest {

    private TriggerRuleRepository ruleRepo;
    private InsurancePlanRepository planRepo;
    private PayoutSlabRepository slabRepo;
    private TriggerRuleServiceImpl ruleService;
    private PayoutSlabServiceImpl slabService;

    @BeforeEach
    void setUp() {
        ruleRepo = mock(TriggerRuleRepository.class);
        planRepo = mock(InsurancePlanRepository.class);
        slabRepo = mock(PayoutSlabRepository.class);
        ruleService = new TriggerRuleServiceImpl(ruleRepo, planRepo);
        slabService = new PayoutSlabServiceImpl(slabRepo, ruleRepo);
    }

    @Test
    void shouldCreateTriggerRule() {
        TriggerRuleRequest request = new TriggerRuleRequest(
                "WHEAT", "SOWING",
                new BigDecimal("30.00"), 6, 14, 7
        );

        when(planRepo.findById(1L)).thenReturn(Mono.just(InsurancePlan.builder().planId(1L).status("ACTIVE").build()));
        when(ruleRepo.existsByPlanIdAndCropTypeIgnoreCaseAndGrowthStageIgnoreCase(1L, "WHEAT", "SOWING"))
                .thenReturn(Mono.just(false));

        TriggerRule saved = TriggerRule.builder()
                .triggerRuleId(10L)
                .planId(1L)
                .cropType("WHEAT")
                .growthStage("SOWING")
                .availabilityThresholdPercentage(new BigDecimal("30.00"))
                .minimumContinuousHours(6)
                .rollingWindowDays(14)
                .requiredShortageDays(7)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        when(ruleRepo.save(any(TriggerRule.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(ruleService.createRule(1L, request))
                .assertNext(res -> {
                    assertThat(res.triggerRuleId()).isEqualTo(10L);
                    assertThat(res.cropType()).isEqualTo("WHEAT");
                })
                .verifyComplete();
    }

    @Test
    void shouldCreatePayoutSlab() {
        PayoutSlabRequest req = new PayoutSlabRequest(
                24, 48, new BigDecimal("20.00"), new BigDecimal("40.00"), new BigDecimal("35.00")
        );

        when(ruleRepo.findById(10L)).thenReturn(Mono.just(TriggerRule.builder().triggerRuleId(10L).build()));
        when(slabRepo.existsByTriggerRuleIdAndMinimumDurationHours(any(), any())).thenReturn(Mono.just(false));

        PayoutSlab saved = PayoutSlab.builder()
                .payoutSlabId(100L)
                .triggerRuleId(10L)
                .minimumDurationHours(24)
                .maximumDurationHours(48)
                .minAvailabilityPct(new BigDecimal("20.00"))
                .maxAvailabilityPct(new BigDecimal("40.00"))
                .payoutPercentage(new BigDecimal("35.00"))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        when(slabRepo.save(any(PayoutSlab.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(slabService.createSlab(10L, req))
                .assertNext(res -> {
                    assertThat(res.payoutSlabId()).isEqualTo(100L);
                    assertThat(res.payoutPercentage()).isEqualByComparingTo("35.00");
                })
                .verifyComplete();
    }

    @Test
    void controllerShouldHandleAdminTriggerRules() {
        TriggerRuleService mockRuleService = mock(TriggerRuleService.class);
        PayoutSlabService mockSlabService = mock(PayoutSlabService.class);
        AdminTriggerRuleController controller = new AdminTriggerRuleController(mockRuleService, mockSlabService);

        TriggerRuleResponse trRes = new TriggerRuleResponse(
                10L, 1L, "WHEAT", "SOWING", new BigDecimal("30.00"), 6, 14, 7, "ACTIVE", LocalDateTime.now(), LocalDateTime.now()
        );
        when(mockRuleService.getRulesForPlan(1L)).thenReturn(Flux.just(trRes));

        StepVerifier.create(controller.getTriggerRules(1L))
                .assertNext(res -> assertThat(res.triggerRuleId()).isEqualTo(10L))
                .verifyComplete();
    }
}
