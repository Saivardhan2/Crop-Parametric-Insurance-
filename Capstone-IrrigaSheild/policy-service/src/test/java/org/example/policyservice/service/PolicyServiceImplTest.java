package org.example.policyservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.Policy;
import org.example.policyservice.entity.PolicyApplication;
import org.example.policyservice.exception.ApiException;
import org.example.policyservice.repository.InsurancePlanRepository;
import org.example.policyservice.repository.PolicyApplicationRepository;
import org.example.policyservice.repository.PolicyRepository;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.underwriting.entity.UnderwritingCase;
import org.example.policyservice.underwriting.repository
        .UnderwritingCaseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyApplicationRepository applicationRepository;

    @Mock
    private InsurancePlanRepository planRepository;

    @Mock
    private UnderwritingCaseRepository caseRepository;

    private PolicyServiceImpl service;
    private AuthenticatedUser farmer;
    private PolicyApplication application;
    private UnderwritingCase underwritingCase;
    private InsurancePlan plan;

    @BeforeEach
    void setUp() {

        service = new PolicyServiceImpl(
                policyRepository,
                applicationRepository,
                planRepository,
                caseRepository
        );

        farmer = new AuthenticatedUser(
                2L,
                "farmer@example.com"
        );

        application = createApplication();
        underwritingCase = createUnderwritingCase();
        plan = createPlan();
    }

    @Test
    void shouldReturnApprovedQuote() {

        mockApprovedData();

        StepVerifier.create(
                        service.getApprovedQuote(farmer, 1L)
                )
                .assertNext(response -> {
                    assertEquals(
                            "QUOTE_APPROVED",
                            response.status()
                    );

                    assertEquals(
                            new BigDecimal("2600.00"),
                            response.premiumAmount()
                    );

                    assertEquals(
                            new BigDecimal("60000.00"),
                            response.coverageAmount()
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldCreatePolicyAfterQuoteAcceptance() {

        when(policyRepository.existsByApplicationId(1L))
                .thenReturn(Mono.just(false));

        mockApprovedData();

        when(applicationRepository.save(
                any(PolicyApplication.class)
        )).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0))
        );

        when(policyRepository.save(any(Policy.class)))
                .thenAnswer(invocation -> {

                    Policy policy = invocation.getArgument(0);
                    policy.setPolicyId(1L);

                    return Mono.just(policy);
                });

        StepVerifier.create(
                        service.acceptQuote(farmer, 1L)
                )
                .assertNext(response -> {
                    assertEquals(1L, response.policyId());
                    assertEquals("SUCCESS", response.paymentStatus());
                    assertEquals("ACTIVE", response.status());
                })
                .verifyComplete();

        assertEquals(
                "POLICY_ACTIVE",
                application.getStatus()
        );
    }

    @Test
    void shouldRejectDuplicatePolicy() {

        when(policyRepository.existsByApplicationId(1L))
                .thenReturn(Mono.just(true));

        StepVerifier.create(
                        service.acceptQuote(farmer, 1L)
                )
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof ApiException);

                    ApiException exception =
                            (ApiException) error;

                    assertEquals(
                            "POLICY_ALREADY_EXISTS",
                            exception.getErrorCode()
                    );
                })
                .verify();

        verify(policyRepository, never())
                .save(any(Policy.class));
    }

    private void mockApprovedData() {

        when(applicationRepository
                .findByApplicationIdAndFarmerUserId(
                        1L,
                        2L
                ))
                .thenReturn(Mono.just(application));

        when(caseRepository.findByApplicationId(1L))
                .thenReturn(Mono.just(underwritingCase));

        when(planRepository.findById(1L))
                .thenReturn(Mono.just(plan));
    }

    private PolicyApplication createApplication() {

        return PolicyApplication.builder()
                .applicationId(1L)
                .farmerUserId(2L)
                .farmerEmail("farmer@example.com")
                .farmId(1L)
                .cropId(1L)
                .planId(1L)
                .requestedCoverage(
                        new BigDecimal("60000.00")
                )
                .proposedStartDate(
                        LocalDate.now().plusDays(1)
                )
                .proposedEndDate(
                        LocalDate.now().plusDays(120)
                )
                .status("APPROVED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private UnderwritingCase createUnderwritingCase() {

        return UnderwritingCase.builder()
                .caseId(1L)
                .applicationId(1L)
                .recommendedPremium(
                        new BigDecimal("2600.00")
                )
                .recommendedCoverage(
                        new BigDecimal("60000.00")
                )
                .decision("APPROVED")
                .status("APPROVED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private InsurancePlan createPlan() {

        return InsurancePlan.builder()
                .planId(1L)
                .planName("IrrigaShield Standard")
                .basePremium(
                        new BigDecimal("2000.00")
                )
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}