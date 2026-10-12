package org.example.policyservice.plan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import org.example.policyservice.controller.AdminPlanController;
import org.example.policyservice.controller.InsurancePlanController;
import org.example.policyservice.dto.request.InsurancePlanRequest;
import org.example.policyservice.dto.response.InsurancePlanResponse;
import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.repository.InsurancePlanRepository;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.service.InsurancePlanService;
import org.example.policyservice.service.InsurancePlanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class InsurancePlanAndAdminControllerTest {

    private InsurancePlanRepository planRepo;
    private InsurancePlanServiceImpl service;
    private AuthenticatedUser adminUser;

    @BeforeEach
    void setUp() {
        planRepo = mock(InsurancePlanRepository.class);
        service = new InsurancePlanServiceImpl(planRepo);
        adminUser = new AuthenticatedUser(1L, "admin@irrigashield.com");
    }

    @Test
    void shouldCreateInsurancePlan() {
        InsurancePlanRequest request = new InsurancePlanRequest(
                "Drought Shield Pro", "Full coverage plan",
                new BigDecimal("50000.00"), new BigDecimal("200000.00"),
                new BigDecimal("2500.00"), 120
        );

        when(planRepo.existsByPlanNameIgnoreCase("Drought Shield Pro")).thenReturn(Mono.just(false));

        InsurancePlan saved = InsurancePlan.builder()
                .planId(10L)
                .planName("Drought Shield Pro")
                .description("Full coverage plan")
                .minimumCoverage(request.minimumCoverage())
                .maximumCoverage(request.maximumCoverage())
                .basePremium(request.basePremium())
                .maximumPolicyDays(120)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        when(planRepo.save(any(InsurancePlan.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.createPlan(adminUser, request))
                .assertNext(res -> {
                    assertThat(res.planId()).isEqualTo(10L);
                    assertThat(res.planName()).isEqualTo("Drought Shield Pro");
                })
                .verifyComplete();
    }

    @Test
    void shouldGetActivePlans() {
        InsurancePlan plan = InsurancePlan.builder()
                .planId(1L)
                .planName("Basic Plan")
                .status("ACTIVE")
                .build();
        when(planRepo.findAllByStatusOrderByCreatedAtDesc("ACTIVE")).thenReturn(Flux.just(plan));

        StepVerifier.create(service.getActivePlans())
                .assertNext(res -> assertThat(res.planName()).isEqualTo("Basic Plan"))
                .verifyComplete();
    }

    @Test
    void shouldHandlePlanControllers() {
        InsurancePlanService mockService = mock(InsurancePlanService.class);
        InsurancePlanController planController = new InsurancePlanController(mockService);
        AdminPlanController adminController = new AdminPlanController(mockService);

        InsurancePlanResponse planRes = new InsurancePlanResponse(
                1L, "Basic Plan", "Desc", new BigDecimal("1000.00"), new BigDecimal("5000.00"),
                new BigDecimal("100.00"), 90, "ACTIVE", LocalDateTime.now(), LocalDateTime.now()
        );

        when(mockService.getActivePlans()).thenReturn(Flux.just(planRes));
        when(mockService.getPlan(1L)).thenReturn(Mono.just(planRes));

        StepVerifier.create(planController.getActivePlans())
                .assertNext(res -> assertThat(res.planId()).isEqualTo(1L))
                .verifyComplete();

        StepVerifier.create(planController.getPlan(1L))
                .assertNext(entity -> assertThat(entity.getBody().planName()).isEqualTo("Basic Plan"))
                .verifyComplete();

        Authentication auth = new UsernamePasswordAuthenticationToken(adminUser, null, Collections.emptyList());
        InsurancePlanRequest req = new InsurancePlanRequest("Basic Plan", "Desc", new BigDecimal("1000.00"), new BigDecimal("5000.00"), new BigDecimal("100.00"), 90);
        when(mockService.createPlan(adminUser, req)).thenReturn(Mono.just(planRes));

        StepVerifier.create(adminController.createPlan(auth, req))
                .assertNext(entity -> assertThat(entity.getBody().planId()).isEqualTo(1L))
                .verifyComplete();
    }
}
