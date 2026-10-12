package org.example.policyservice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.policyservice.controller.InternalPolicyController;
import org.example.policyservice.dto.response.InsurancePlanResponse;
import org.example.policyservice.dto.response.PayoutSlabResponse;
import org.example.policyservice.dto.response.PolicyApplicationResponse;
import org.example.policyservice.dto.response.TriggerRuleResponse;
import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.PayoutSlab;
import org.example.policyservice.entity.Policy;
import org.example.policyservice.entity.PolicyApplication;
import org.example.policyservice.entity.TriggerRule;
import org.example.policyservice.repository.PayoutSlabRepository;
import org.example.policyservice.repository.PolicyRepository;
import org.example.policyservice.repository.TriggerRuleRepository;
import org.example.policyservice.util.InsurancePlanMapper;
import org.example.policyservice.util.PayoutSlabMapper;
import org.example.policyservice.util.PolicyApplicationMapper;
import org.example.policyservice.util.TriggerRuleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PolicySecurityAndMapperTest {

    @Test
    void shouldFilterTrustedHeaders() {
        TrustedHeaderAuthenticationFilter filter = new TrustedHeaderAuthenticationFilter();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/policies")
                .header("X-User-Id", "99")
                .header("X-User-Email", "uw@irrigashield.com")
                .header("X-User-Roles", "ROLE_UNDERWRITER")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void shouldHandleInternalPolicyController() {
        PolicyRepository policyRepo = mock(PolicyRepository.class);
        TriggerRuleRepository ruleRepo = mock(TriggerRuleRepository.class);
        PayoutSlabRepository slabRepo = mock(PayoutSlabRepository.class);
        InternalPolicyController controller = new InternalPolicyController(policyRepo, ruleRepo, slabRepo);

        Policy policy = Policy.builder()
                .policyId(10L)
                .applicationId(100L)
                .farmerUserId(50L)
                .farmId(10L)
                .cropId(20L)
                .planId(1L)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("2000.00"))
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(80))
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        TriggerRule rule = TriggerRule.builder()
                .triggerRuleId(1L)
                .planId(1L)
                .cropType("WHEAT")
                .growthStage("SOWING")
                .availabilityThresholdPercentage(new BigDecimal("30.00"))
                .status("ACTIVE")
                .build();

        PayoutSlab slab = PayoutSlab.builder()
                .payoutSlabId(5L)
                .triggerRuleId(1L)
                .payoutPercentage(new BigDecimal("50.00"))
                .status("ACTIVE")
                .build();

        when(policyRepo.findByPolicyIdAndStatus(10L, "ACTIVE")).thenReturn(Mono.just(policy));
        when(ruleRepo.findAllByPlanIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE")).thenReturn(Flux.just(rule));
        when(slabRepo.findAllByTriggerRuleIdAndStatus(1L, "ACTIVE")).thenReturn(Flux.just(slab));

        StepVerifier.create(controller.getActivePolicy(10L))
                .assertNext(res -> assertThat(res.policyId()).isEqualTo(10L))
                .verifyComplete();

        StepVerifier.create(controller.getPolicyClaimConfiguration(10L))
                .assertNext(res -> {
                    assertThat(res.policyId()).isEqualTo(10L);
                    assertThat(res.triggerRule()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void shouldMapEntitiesCorrectly() {
        InsurancePlan plan = InsurancePlan.builder().planId(1L).planName("P1").build();
        InsurancePlanResponse ipr = InsurancePlanMapper.toResponse(plan);
        assertThat(ipr.planName()).isEqualTo("P1");

        PayoutSlab slab = PayoutSlab.builder().payoutSlabId(2L).payoutPercentage(new BigDecimal("25.00")).build();
        PayoutSlabResponse psr = PayoutSlabMapper.toResponse(slab);
        assertThat(psr.payoutPercentage()).isEqualByComparingTo("25.00");

        TriggerRule rule = TriggerRule.builder().triggerRuleId(3L).cropType("RICE").build();
        TriggerRuleResponse trr = TriggerRuleMapper.toResponse(rule);
        assertThat(trr.cropType()).isEqualTo("RICE");

        PolicyApplication app = PolicyApplication.builder().applicationId(4L).status("DRAFT").build();
        PolicyApplicationResponse par = PolicyApplicationMapper.toResponse(app, plan);
        assertThat(par.status()).isEqualTo("DRAFT");
    }
}
