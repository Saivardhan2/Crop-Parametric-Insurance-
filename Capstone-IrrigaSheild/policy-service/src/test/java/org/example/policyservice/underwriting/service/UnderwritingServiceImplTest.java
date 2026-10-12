package org.example.policyservice.underwriting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.policyservice.client.FarmServiceClient;
import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.PolicyApplication;
import org.example.policyservice.repository.InsurancePlanRepository;
import org.example.policyservice.repository.PolicyApplicationRepository;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.underwriting.dto.request.DecisionRequest;
import org.example.policyservice.underwriting.dto.response
        .FarmRiskProfileResponse;
import org.example.policyservice.underwriting.entity.UnderwritingCase;
import org.example.policyservice.underwriting.repository
        .UnderwritingCaseRepository;
import org.example.policyservice.underwriting.util.AssessmentResult;
import org.example.policyservice.underwriting.util.RiskCalculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UnderwritingServiceImplTest {

    @Mock
    private PolicyApplicationRepository applicationRepository;

    @Mock
    private InsurancePlanRepository planRepository;

    @Mock
    private UnderwritingCaseRepository caseRepository;

    @Mock
    private FarmServiceClient farmServiceClient;

    @Mock
    private RiskCalculator riskCalculator;

    private UnderwritingServiceImpl service;
    private AuthenticatedUser underwriter;

    @BeforeEach
    void setUp() {

        service = new UnderwritingServiceImpl(
                applicationRepository,
                planRepository,
                caseRepository,
                farmServiceClient,
                riskCalculator
        );

        underwriter = new AuthenticatedUser(
                3L,
                "underwriter@irrigashield.com"
        );
    }

    @Test
    void shouldStartUnderwritingReview() {

        PolicyApplication application =
                createApplication("SUBMITTED");

        InsurancePlan plan = createPlan();

        when(caseRepository.existsByApplicationId(1L))
                .thenReturn(Mono.just(false));

        when(applicationRepository.findById(1L))
                .thenReturn(Mono.just(application));

        when(applicationRepository.save(
                any(PolicyApplication.class)
        )).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0))
        );

        when(caseRepository.save(
                any(UnderwritingCase.class)
        )).thenAnswer(invocation -> {

            UnderwritingCase underwritingCase =
                    invocation.getArgument(0);

            underwritingCase.setCaseId(1L);

            return Mono.just(underwritingCase);
        });

        when(planRepository.findById(1L))
                .thenReturn(Mono.just(plan));

        StepVerifier.create(
                        service.startReview(
                                underwriter,
                                1L
                        )
                )
                .assertNext(response -> {
                    assertEquals(1L, response.caseId());

                    assertEquals(
                            "UNDER_REVIEW",
                            response.applicationStatus()
                    );

                    assertEquals(
                            "UNDER_REVIEW",
                            response.underwritingStatus()
                    );

                    assertEquals(
                            3L,
                            response.underwriterUserId()
                    );

                    assertNotNull(
                            response.reviewStartedAt()
                    );
                })
                .verifyComplete();

        assertEquals(
                "UNDER_REVIEW",
                application.getStatus()
        );
    }

    @Test
    void shouldAssessRiskAndRecommendPremium() {

        PolicyApplication application =
                createApplication("UNDER_REVIEW");

        UnderwritingCase underwritingCase =
                createUnderwritingCase();

        InsurancePlan plan = createPlan();

        FarmRiskProfileResponse riskProfile =
                org.mockito.Mockito.mock(
                        FarmRiskProfileResponse.class
                );

        AssessmentResult assessment =
                new AssessmentResult(
                        20,
                        10,
                        10,
                        15,
                        0,
                        55,
                        "HIGH",
                        new BigDecimal("2000.00"),
                        new BigDecimal("600.00"),
                        new BigDecimal("2600.00"),
                        new BigDecimal("60000.00")
                );

        when(caseRepository.findByApplicationId(1L))
                .thenReturn(Mono.just(underwritingCase));

        when(applicationRepository.findById(1L))
                .thenReturn(Mono.just(application));

        when(planRepository.findById(1L))
                .thenReturn(Mono.just(plan));

        when(farmServiceClient.getFarmRiskProfile(
                underwriter,
                1L
        )).thenReturn(Mono.just(riskProfile));

        when(riskCalculator.assess(
                plan,
                application,
                riskProfile
        )).thenReturn(assessment);

        when(caseRepository.save(
                any(UnderwritingCase.class)
        )).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0))
        );

        StepVerifier.create(
                        service.assess(
                                underwriter,
                                1L
                        )
                )
                .assertNext(response -> {
                    assertEquals(
                            55,
                            response.overallRiskScore()
                    );

                    assertEquals(
                            "HIGH",
                            response.riskLevel()
                    );

                    assertEquals(
                            new BigDecimal("600.00"),
                            response.riskLoading()
                    );

                    assertEquals(
                            new BigDecimal("2600.00"),
                            response.recommendedPremium()
                    );

                    assertEquals(
                            new BigDecimal("60000.00"),
                            response.recommendedCoverage()
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldApproveAssessedApplication() {

        PolicyApplication application =
                createApplication("UNDER_REVIEW");

        UnderwritingCase underwritingCase =
                createUnderwritingCase();

        underwritingCase.setOverallRiskScore(55);
        underwritingCase.setRiskLevel("HIGH");
        underwritingCase.setRecommendedPremium(
                new BigDecimal("2600.00")
        );
        underwritingCase.setRecommendedCoverage(
                new BigDecimal("60000.00")
        );

        InsurancePlan plan = createPlan();

        DecisionRequest request =
                new DecisionRequest(
                        "Risk is acceptable at the recommended premium"
                );

        when(caseRepository.findByApplicationId(1L))
                .thenReturn(Mono.just(underwritingCase));

        when(applicationRepository.findById(1L))
                .thenReturn(Mono.just(application));

        when(applicationRepository.save(
                any(PolicyApplication.class)
        )).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0))
        );

        when(caseRepository.save(
                any(UnderwritingCase.class)
        )).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0))
        );

        when(planRepository.findById(1L))
                .thenReturn(Mono.just(plan));

        StepVerifier.create(
                        service.approve(
                                underwriter,
                                1L,
                                request
                        )
                )
                .assertNext(response -> {
                    assertEquals(
                            "APPROVED",
                            response.applicationStatus()
                    );

                    assertEquals(
                            "APPROVED",
                            response.underwritingStatus()
                    );

                    assertEquals(
                            "APPROVED",
                            response.decision()
                    );

                    assertEquals(
                            request.remarks(),
                            response.remarks()
                    );

                    assertNotNull(
                            response.reviewCompletedAt()
                    );
                })
                .verifyComplete();

        assertEquals(
                "APPROVED",
                application.getStatus()
        );

        assertEquals(
                "APPROVED",
                underwritingCase.getStatus()
        );
    }

    private PolicyApplication createApplication(
            String status) {

        LocalDateTime now = LocalDateTime.now();

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
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private UnderwritingCase createUnderwritingCase() {

        LocalDateTime now = LocalDateTime.now();

        return UnderwritingCase.builder()
                .caseId(1L)
                .applicationId(1L)
                .underwriterUserId(3L)
                .underwriterEmail(
                        "underwriter@irrigashield.com"
                )
                .status("UNDER_REVIEW")
                .reviewStartedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private InsurancePlan createPlan() {

        LocalDateTime now = LocalDateTime.now();

        return InsurancePlan.builder()
                .planId(1L)
                .planName("IrrigaShield Standard")
                .description(
                        "Parametric irrigation insurance plan"
                )
                .minimumCoverage(
                        new BigDecimal("10000.00")
                )
                .maximumCoverage(
                        new BigDecimal("100000.00")
                )
                .basePremium(
                        new BigDecimal("2000.00")
                )
                .maximumPolicyDays(180)
                .status("ACTIVE")
                .createdBy(1L)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}