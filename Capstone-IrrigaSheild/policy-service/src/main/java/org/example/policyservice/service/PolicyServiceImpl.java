package org.example.policyservice.service;

import java.time.LocalDateTime;

import org.example.policyservice.dto.response.PolicyResponse;
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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PolicyServiceImpl implements PolicyService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PolicyServiceImpl.class);

    private static final String APPROVED = "APPROVED";
    private static final String POLICY_ACTIVE = "POLICY_ACTIVE";
    private static final String PAYMENT_SUCCESS = "SUCCESS";
    private static final String ACTIVE = "ACTIVE";

    private final PolicyRepository policyRepository;
    private final PolicyApplicationRepository applicationRepository;
    private final InsurancePlanRepository planRepository;
    private final UnderwritingCaseRepository caseRepository;

    public PolicyServiceImpl(
            PolicyRepository policyRepository,
            PolicyApplicationRepository applicationRepository,
            InsurancePlanRepository planRepository,
            UnderwritingCaseRepository caseRepository) {

        this.policyRepository = policyRepository;
        this.applicationRepository = applicationRepository;
        this.planRepository = planRepository;
        this.caseRepository = caseRepository;
    }

    @Override
    public Mono<PolicyResponse> getApprovedQuote(
            AuthenticatedUser farmer,
            Long applicationId) {

        return findOwnedApplication(
                farmer.userId(),
                applicationId
        )
                .flatMap(application -> {

                    if (!APPROVED.equals(application.getStatus())) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "APPLICATION_NOT_APPROVED",
                                        "The application has not been approved"
                                )
                        );
                    }

                    return Mono.zip(
                            findApprovedCase(applicationId),
                            findPlan(application.getPlanId())
                    ).map(result -> createQuoteResponse(
                            application,
                            result.getT1(),
                            result.getT2()
                    ));
                });
    }

    @Override
    public Mono<PolicyResponse> acceptQuote(
            AuthenticatedUser farmer,
            Long applicationId) {

        return policyRepository
                .existsByApplicationId(applicationId)
                .flatMap(policyExists -> {

                    if (policyExists) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "POLICY_ALREADY_EXISTS",
                                        "A policy already exists for this application"
                                )
                        );
                    }

                    return findOwnedApplication(
                            farmer.userId(),
                            applicationId
                    );
                })
                .flatMap(application -> {

                    if (!APPROVED.equals(application.getStatus())) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "APPLICATION_NOT_APPROVED",
                                        "Only approved applications can be accepted"
                                )
                        );
                    }

                    return Mono.zip(
                            findApprovedCase(applicationId),
                            findPlan(application.getPlanId())
                    ).flatMap(result -> {

                        UnderwritingCase underwritingCase =
                                result.getT1();

                        InsurancePlan plan = result.getT2();

                        LocalDateTime now =
                                LocalDateTime.now();

                        Policy policy = Policy.builder()
                                .applicationId(
                                        application.getApplicationId()
                                )
                                .farmerUserId(
                                        application.getFarmerUserId()
                                )
                                .farmerEmail(
                                        application.getFarmerEmail()
                                )
                                .farmId(application.getFarmId())
                                .cropId(application.getCropId())
                                .planId(application.getPlanId())
                                .coverageAmount(
                                        underwritingCase
                                                .getRecommendedCoverage()
                                )
                                .premiumAmount(
                                        underwritingCase
                                                .getRecommendedPremium()
                                )
                                .startDate(
                                        application
                                                .getProposedStartDate()
                                )
                                .endDate(
                                        application
                                                .getProposedEndDate()
                                )
                                .paymentStatus(PAYMENT_SUCCESS)
                                .status(ACTIVE)
                                .acceptedAt(now)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                        application.setStatus(POLICY_ACTIVE);
                        application.setUpdatedAt(now);

                        return applicationRepository
                                .save(application)
                                .then(policyRepository.save(policy))
                                .map(savedPolicy ->
                                        toResponse(
                                                savedPolicy,
                                                plan
                                        )
                                );
                    });
                });
    }

    @Override
    public Flux<PolicyResponse> getMyPolicies(
            AuthenticatedUser farmer) {

        return policyRepository
                .findAllByFarmerUserIdOrderByCreatedAtDesc(
                        farmer.userId()
                )
                .flatMap(policy ->
                        findPlan(policy.getPlanId())
                                .map(plan ->
                                        toResponse(
                                                policy,
                                                plan
                                        )
                                )
                );
    }

    private Mono<PolicyApplication> findOwnedApplication(
            Long farmerUserId,
            Long applicationId) {

        return applicationRepository
                .findByApplicationIdAndFarmerUserId(
                        applicationId,
                        farmerUserId
                )
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

    private Mono<UnderwritingCase> findApprovedCase(
            Long applicationId) {

        return caseRepository
                .findByApplicationId(applicationId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "UNDERWRITING_CASE_NOT_FOUND",
                                        "Underwriting case was not found"
                                )
                        )
                )
                .flatMap(underwritingCase -> {

                    if (!APPROVED.equals(
                            underwritingCase.getDecision())) {

                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "QUOTE_NOT_APPROVED",
                                        "The underwriting quote is not approved"
                                )
                        );
                    }

                    if (underwritingCase
                            .getRecommendedPremium() == null
                            || underwritingCase
                            .getRecommendedCoverage() == null) {

                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "QUOTE_INCOMPLETE",
                                        "The approved quote is incomplete"
                                )
                        );
                    }

                    return Mono.just(underwritingCase);
                });
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

    private PolicyResponse createQuoteResponse(
            PolicyApplication application,
            UnderwritingCase underwritingCase,
            InsurancePlan plan) {

        return new PolicyResponse(
                null,
                application.getApplicationId(),
                application.getFarmerUserId(),
                application.getFarmerEmail(),
                application.getFarmId(),
                application.getCropId(),
                plan.getPlanId(),
                plan.getPlanName(),
                underwritingCase.getRecommendedCoverage(),
                underwritingCase.getRecommendedPremium(),
                application.getProposedStartDate(),
                application.getProposedEndDate(),
                "PENDING",
                "QUOTE_APPROVED",
                null
        );
    }

    private PolicyResponse toResponse(
            Policy policy,
            InsurancePlan plan) {

        return new PolicyResponse(
                policy.getPolicyId(),
                policy.getApplicationId(),
                policy.getFarmerUserId(),
                policy.getFarmerEmail(),
                policy.getFarmId(),
                policy.getCropId(),
                policy.getPlanId(),
                plan.getPlanName(),
                policy.getCoverageAmount(),
                policy.getPremiumAmount(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getPaymentStatus(),
                policy.getStatus(),
                policy.getAcceptedAt()
        );
    }
}