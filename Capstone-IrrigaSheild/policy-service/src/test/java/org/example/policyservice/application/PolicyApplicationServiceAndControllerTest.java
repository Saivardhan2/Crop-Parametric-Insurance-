package org.example.policyservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import org.example.policyservice.client.FarmServiceClient;
import org.example.policyservice.controller.PolicyApplicationController;
import org.example.policyservice.dto.request.PolicyApplicationRequest;
import org.example.policyservice.dto.response.FarmApplicationValidationResponse;
import org.example.policyservice.dto.response.PolicyApplicationResponse;
import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.PolicyApplication;
import org.example.policyservice.repository.InsurancePlanRepository;
import org.example.policyservice.repository.PolicyApplicationRepository;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.service.PolicyApplicationService;
import org.example.policyservice.service.PolicyApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PolicyApplicationServiceAndControllerTest {

    private FarmServiceClient farmServiceClient;
    private PolicyApplicationRepository applicationRepo;
    private InsurancePlanRepository planRepo;
    private PolicyApplicationServiceImpl service;
    private AuthenticatedUser user;

    @BeforeEach
    void setUp() {
        farmServiceClient = mock(FarmServiceClient.class);
        applicationRepo = mock(PolicyApplicationRepository.class);
        planRepo = mock(InsurancePlanRepository.class);
        service = new PolicyApplicationServiceImpl(farmServiceClient, applicationRepo, planRepo);
        user = new AuthenticatedUser(50L, "farmer50@test.com");
    }

    @Test
    void shouldCreateApplicationSuccessfully() {
        PolicyApplicationRequest request = new PolicyApplicationRequest(
                10L, 20L, 1L, new BigDecimal("100000.00"),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(60)
        );

        InsurancePlan plan = InsurancePlan.builder()
                .planId(1L)
                .status("ACTIVE")
                .minimumCoverage(new BigDecimal("50000.00"))
                .maximumCoverage(new BigDecimal("200000.00"))
                .maximumPolicyDays(90)
                .basePremium(new BigDecimal("1500.00"))
                .build();

        FarmApplicationValidationResponse valRes = new FarmApplicationValidationResponse(
                10L, 20L, 50L, "My Farm", "WHEAT", "SOWING", true, true
        );

        when(planRepo.findById(1L)).thenReturn(Mono.just(plan));
        when(farmServiceClient.validateFarmAndCrop(user, 10L, 20L)).thenReturn(Mono.just(valRes));
        when(applicationRepo.existsByFarmerUserIdAndFarmIdAndStatusIn(any(), any(), any())).thenReturn(Mono.just(false));

        PolicyApplication saved = PolicyApplication.builder()
                .applicationId(101L)
                .farmerUserId(50L)
                .farmerEmail("farmer50@test.com")
                .farmId(10L)
                .cropId(20L)
                .planId(1L)
                .requestedCoverage(new BigDecimal("100000.00"))
                .status("DRAFT")
                .createdAt(LocalDateTime.now())
                .build();
        when(applicationRepo.save(any(PolicyApplication.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.createApplication(user, request))
                .assertNext(res -> {
                    assertThat(res.applicationId()).isEqualTo(101L);
                    assertThat(res.status()).isEqualTo("DRAFT");
                })
                .verifyComplete();
    }

    @Test
    void controllerShouldHandleApplicationEndpoints() {
        PolicyApplicationService mockService = mock(PolicyApplicationService.class);
        PolicyApplicationController controller = new PolicyApplicationController(mockService);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());

        PolicyApplicationResponse appRes = new PolicyApplicationResponse(
                101L, 50L, "farmer50@test.com", 10L, 20L, 1L, "Basic Plan", new BigDecimal("100000.00"),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(60),
                "SUBMITTED", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(mockService.getMyApplications(user)).thenReturn(Flux.just(appRes));
        when(mockService.submitApplication(user, 101L)).thenReturn(Mono.just(appRes));

        StepVerifier.create(controller.getMyApplications(auth))
                .assertNext(res -> assertThat(res.applicationId()).isEqualTo(101L))
                .verifyComplete();

        StepVerifier.create(controller.submitApplication(auth, 101L))
                .assertNext(entity -> assertThat(entity.getBody().status()).isEqualTo("SUBMITTED"))
                .verifyComplete();
    }
}
