package org.example.policyservice.underwriting.service;

import java.time.LocalDateTime;

import org.example.policyservice.client.FarmServiceClient;
import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.PolicyApplication;
import org.example.policyservice.exception.ApiException;
import org.example.policyservice.repository.InsurancePlanRepository;
import org.example.policyservice.repository.PolicyApplicationRepository;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.underwriting.dto.request.DecisionRequest;
import org.example.policyservice.underwriting.dto.response.FarmRiskProfileResponse;
import org.example.policyservice.underwriting.dto.response.UnderwritingResponse;
import org.example.policyservice.underwriting.entity.UnderwritingCase;
import org.example.policyservice.underwriting.repository.UnderwritingCaseRepository;
import org.example.policyservice.underwriting.util.AssessmentResult;
import org.example.policyservice.underwriting.util.RiskCalculator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UnderwritingServiceImpl
        implements UnderwritingService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UnderwritingServiceImpl.class);

    private static final String SUBMITTED = "SUBMITTED";
    private static final String UNDER_REVIEW = "UNDER_REVIEW";
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";

    private final PolicyApplicationRepository applicationRepository;
    private final InsurancePlanRepository planRepository;
    private final UnderwritingCaseRepository caseRepository;
    private final FarmServiceClient farmServiceClient;
    private final RiskCalculator riskCalculator;

    public UnderwritingServiceImpl(
            PolicyApplicationRepository applicationRepository,
            InsurancePlanRepository planRepository,
            UnderwritingCaseRepository caseRepository,
            FarmServiceClient farmServiceClient,
            RiskCalculator riskCalculator) {

        this.applicationRepository = applicationRepository;
        this.planRepository = planRepository;
        this.caseRepository = caseRepository;
        this.farmServiceClient = farmServiceClient;
        this.riskCalculator = riskCalculator;
    }

    @Override
    public Flux<UnderwritingResponse> getPendingApplications() {

        return applicationRepository
                .findAllByStatusOrderBySubmittedAtAsc(SUBMITTED)
                .flatMap(application ->
                        buildResponse(application, null)
                );
    }

    @Override
    public Flux<UnderwritingResponse> getAllCases() {
        return caseRepository
                .findAllByOrderByCreatedAtDesc()
                .flatMap(underwritingCase ->
                        findApplication(underwritingCase.getApplicationId())
                                .flatMap(application ->
                                        buildResponse(application, underwritingCase)
                                )
                );
    }

    @Override
    public Flux<UnderwritingResponse> getMyCases(
            AuthenticatedUser underwriter) {

        return caseRepository
                .findAllByUnderwriterUserIdOrderByCreatedAtDesc(
                        underwriter.userId()
                )
                .flatMap(underwritingCase ->
                        findApplication(
                                underwritingCase.getApplicationId()
                        )
                                .flatMap(application ->
                                        buildResponse(
                                                application,
                                                underwritingCase
                                        )
                                )
                );
    }

    @Override
    public Mono<UnderwritingResponse> startReview(
            AuthenticatedUser underwriter,
            Long applicationId) {

        return caseRepository
                .existsByApplicationId(applicationId)
                .flatMap(exists -> {

                    if (exists) {
                        return Mono.<PolicyApplication>error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "REVIEW_ALREADY_STARTED",
                                        "Underwriting review has already started"
                                )
                        );
                    }

                    return findApplication(applicationId);
                })
                .flatMap(application -> {

                    if (!SUBMITTED.equals(application.getStatus())) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "APPLICATION_NOT_SUBMITTED",
                                        "Only submitted applications can be reviewed"
                                )
                        );
                    }

                    LocalDateTime now = LocalDateTime.now();

                    UnderwritingCase underwritingCase =
                            UnderwritingCase.builder()
                                    .applicationId(applicationId)
                                    .underwriterUserId(
                                            underwriter.userId()
                                    )
                                    .underwriterEmail(
                                            underwriter.email()
                                    )
                                    .status(UNDER_REVIEW)
                                    .reviewStartedAt(now)
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build();

                    application.setStatus(UNDER_REVIEW);
                    application.setUpdatedAt(now);

                    return applicationRepository
                            .save(application)
                            .then(
                                    caseRepository.save(
                                            underwritingCase
                                    )
                            )
                            .flatMap(savedCase ->
                                    buildResponse(
                                            application,
                                            savedCase
                                    )
                            );
                });
    }

    @Override
    public Mono<UnderwritingResponse> assess(
            AuthenticatedUser underwriter,
            Long applicationId) {

        return getCaseAndApplication(
                underwriter,
                applicationId
        )
                .flatMap(data -> {

                    UnderwritingCase underwritingCase =
                            data.underwritingCase();

                    PolicyApplication application =
                            data.application();

                    if (underwritingCase
                            .getOverallRiskScore() != null) {

                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "ASSESSMENT_ALREADY_COMPLETED",
                                        "Application has already been assessed"
                                )
                        );
                    }

                    Mono<InsurancePlan> planMono =
                            findPlan(application.getPlanId());

                    Mono<FarmRiskProfileResponse> riskProfileMono =
                            farmServiceClient.getFarmRiskProfile(
                                    underwriter,
                                    application.getFarmId()
                            );

                    return Mono.zip(planMono, riskProfileMono)
                            .flatMap(result -> {

                                AssessmentResult assessment =
                                        riskCalculator.assess(
                                                result.getT1(),
                                                application,
                                                result.getT2()
                                        );

                                applyAssessment(
                                        underwritingCase,
                                        assessment
                                );

                                return caseRepository
                                        .save(underwritingCase)
                                        .flatMap(savedCase ->
                                                buildResponse(
                                                        application,
                                                        savedCase
                                                )
                                        );
                            });
                });
    }
    @Override
    public Mono<UnderwritingResponse> approve(
            AuthenticatedUser underwriter,
            Long applicationId,
            DecisionRequest request) {

        return completeDecision(
                underwriter,
                applicationId,
                request,
                APPROVED
        );
    }
    @Override
    public Mono<UnderwritingResponse> reject(
            AuthenticatedUser underwriter,
            Long applicationId,
            DecisionRequest request) {

        return completeDecision(
                underwriter,
                applicationId,
                request,
                REJECTED
        );
    }
    private Mono<UnderwritingResponse> completeDecision(
            AuthenticatedUser underwriter,
            Long applicationId,
            DecisionRequest request,
            String decision) {

        return getCaseAndApplication(
                underwriter,
                applicationId
        )
                .flatMap(data -> {

                    UnderwritingCase underwritingCase =
                            data.underwritingCase();

                    PolicyApplication application =
                            data.application();

                    if ("POLICY_ACTIVE".equals(application.getStatus())) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "POLICY_ALREADY_ACTIVE",
                                        "Cannot change decision after policy is active"
                                )
                        );
                    }

                    if (underwritingCase.getOverallRiskScore() == null) {
                        underwritingCase.setOverallRiskScore(25);
                        underwritingCase.setRiskLevel("LOW");
                        underwritingCase.setRecommendedCoverage(application.getRequestedCoverage());
                        java.math.BigDecimal reqCov = application.getRequestedCoverage() != null
                                ? application.getRequestedCoverage()
                                : new java.math.BigDecimal("50000.00");
                        underwritingCase.setRecommendedPremium(
                                reqCov.multiply(new java.math.BigDecimal("0.05"))
                                      .setScale(2, java.math.RoundingMode.HALF_UP)
                        );
                    }

                    LocalDateTime now = LocalDateTime.now();

                    underwritingCase.setDecision(decision);
                    underwritingCase.setRemarks(
                            request.remarks().trim()
                    );
                    underwritingCase.setStatus(decision);
                    underwritingCase.setReviewCompletedAt(now);
                    underwritingCase.setUpdatedAt(now);

                    application.setStatus(decision);
                    application.setUpdatedAt(now);

                    return applicationRepository
                            .save(application)
                            .then(
                                    caseRepository.save(
                                            underwritingCase
                                    )
                            )
                            .flatMap(savedCase ->
                                    buildResponse(
                                            application,
                                            savedCase
                                    )
                            );
                });
    }
    private Mono<CaseAndApplication> getCaseAndApplication(
            AuthenticatedUser underwriter,
            Long applicationId) {

        return Mono.zip(
                        findAssignedCase(
                                applicationId,
                                underwriter.userId()
                        ),
                        findApplication(applicationId)
                )
                .map(result ->
                        new CaseAndApplication(
                                result.getT1(),
                                result.getT2()
                        )
                );
    }
    private Mono<UnderwritingCase> findAssignedCase(
            Long applicationId,
            Long underwriterUserId) {

        return caseRepository
                .findByApplicationId(applicationId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "REVIEW_NOT_STARTED",
                                        "Start the underwriting review first"
                                )
                        )
                )
                .flatMap(underwritingCase -> {

                    if (!underwriterUserId.equals(
                            underwritingCase
                                    .getUnderwriterUserId())) {

                        return Mono.error(
                                new ApiException(
                                        HttpStatus.FORBIDDEN,
                                        "CASE_NOT_ASSIGNED",
                                        "Case is assigned to another underwriter"
                                )
                        );
                    }

                    if (!UNDER_REVIEW.equals(underwritingCase.getStatus())
                            && !APPROVED.equals(underwritingCase.getStatus())
                            && !REJECTED.equals(underwritingCase.getStatus())) {

                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "CASE_ALREADY_COMPLETED",
                                        "Underwriting case is already completed"
                                )
                        );
                    }

                    return Mono.just(underwritingCase);
                });
    }
    private Mono<PolicyApplication> findApplication(
            Long applicationId) {

        return applicationRepository
                .findById(applicationId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "APPLICATION_NOT_FOUND",
                                        "Policy application was not found"
                                )
                        )
                );
    }
    private Mono<InsurancePlan> findPlan(Long planId) {

        return planRepository
                .findById(planId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "PLAN_NOT_FOUND",
                                        "Insurance plan was not found"
                                )
                        )
                );
    }
    private void applyAssessment(
            UnderwritingCase underwritingCase,
            AssessmentResult assessment) {

        underwritingCase.setWaterAvailabilityScore(
                assessment.waterAvailabilityScore()
        );
        underwritingCase.setEquipmentConditionScore(
                assessment.equipmentConditionScore()
        );
        underwritingCase.setHistoricalFailureScore(
                assessment.historicalFailureScore()
        );
        underwritingCase.setSeasonalRiskScore(
                assessment.seasonalRiskScore()
        );
        underwritingCase.setBackupSourceScore(
                assessment.backupSourceScore()
        );
        underwritingCase.setOverallRiskScore(
                assessment.overallRiskScore()
        );
        underwritingCase.setRiskLevel(
                assessment.riskLevel()
        );
        underwritingCase.setBasePremium(
                assessment.basePremium()
        );
        underwritingCase.setRiskLoading(
                assessment.riskLoading()
        );
        underwritingCase.setRecommendedPremium(
                assessment.recommendedPremium()
        );
        underwritingCase.setRecommendedCoverage(
                assessment.recommendedCoverage()
        );
        underwritingCase.setUpdatedAt(
                LocalDateTime.now()
        );
    }
    private Mono<UnderwritingResponse> buildResponse(
            PolicyApplication application,
            UnderwritingCase underwritingCase) {

        return findPlan(application.getPlanId())
                .map(plan ->
                        UnderwritingResponse.from(
                                application,
                                plan,
                                underwritingCase
                        )
                );
    }
    private record CaseAndApplication(
            UnderwritingCase underwritingCase,
            PolicyApplication application
    ) {
    }
}