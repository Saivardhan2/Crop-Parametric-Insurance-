package org.example.policyservice.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.example.policyservice.dto.request.PolicyApplicationRequest;
import org.example.policyservice.dto.response.PolicyApplicationResponse;
import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.PolicyApplication;
import org.example.policyservice.exception.ApiException;
import org.example.policyservice.repository.InsurancePlanRepository;
import org.example.policyservice.repository.PolicyApplicationRepository;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.util.PolicyApplicationMapper;
import org.example.policyservice.client.FarmServiceClient;
import org.example.policyservice.dto.response
        .FarmApplicationValidationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PolicyApplicationServiceImpl
        implements PolicyApplicationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PolicyApplicationServiceImpl.class);

    private final FarmServiceClient farmServiceClient;
    private static final String DRAFT = "DRAFT";
    private static final String SUBMITTED = "SUBMITTED";

    private static final List<String> ACTIVE_STATUSES =
            List.of(
                    DRAFT,
                    SUBMITTED,
                    "UNDER_REVIEW",
                    "INFORMATION_REQUIRED",
                    "APPROVED",
                    "PAYMENT_PENDING"
            );

    private final PolicyApplicationRepository applicationRepository;
    private final InsurancePlanRepository planRepository;

    public PolicyApplicationServiceImpl(
            FarmServiceClient farmServiceClient, PolicyApplicationRepository applicationRepository,
            InsurancePlanRepository planRepository) {
        this.farmServiceClient = farmServiceClient;

        this.applicationRepository = applicationRepository;
        this.planRepository = planRepository;
    }

    @Override
    public Mono<PolicyApplicationResponse> createApplication(
            AuthenticatedUser user,
            PolicyApplicationRequest request) {

        Mono<InsurancePlan> planValidation =
                validatePlanAndRequest(request);

        Mono<FarmApplicationValidationResponse> farmValidation =
                farmServiceClient.validateFarmAndCrop(
                        user,
                        request.farmId(),
                        request.cropId()
                );

        return Mono.zip(
                        planValidation,
                        farmValidation
                )
                .flatMap(validationResult -> {

                    InsurancePlan plan =
                            validationResult.getT1();

                    FarmApplicationValidationResponse
                            verifiedFarm =
                            validationResult.getT2();

                    if (!verifiedFarm.eligible()) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "FARM_NOT_ELIGIBLE",
                                        "Selected farm is not eligible"
                                )
                        );
                    }

                    return applicationRepository
                            .existsByFarmerUserIdAndFarmIdAndStatusIn(
                                    user.userId(),
                                    request.farmId(),
                                    ACTIVE_STATUSES
                            )
                            .flatMap(exists -> {

                                if (exists) {
                                    return Mono.error(
                                            new ApiException(
                                                    HttpStatus.CONFLICT,
                                                    "ACTIVE_APPLICATION_EXISTS",
                                                    "An active application already exists for this farm"
                                            )
                                    );
                                }

                                LocalDateTime now =
                                        LocalDateTime.now();

                                PolicyApplication application =
                                        PolicyApplication.builder()
                                                .farmerUserId(
                                                        user.userId()
                                                )
                                                .farmerEmail(
                                                        user.email()
                                                )
                                                .farmId(
                                                        verifiedFarm.farmId()
                                                )
                                                .cropId(
                                                        verifiedFarm.cropId()
                                                )
                                                .planId(
                                                        request.planId()
                                                )
                                                .requestedCoverage(
                                                        request.requestedCoverage()
                                                )
                                                .proposedStartDate(
                                                        request.proposedStartDate()
                                                )
                                                .proposedEndDate(
                                                        request.proposedEndDate()
                                                )
                                                .status(DRAFT)
                                                .createdAt(now)
                                                .updatedAt(now)
                                                .build();

                                return applicationRepository
                                        .save(application)
                                        .map(saved ->
                                                PolicyApplicationMapper
                                                        .toResponse(
                                                                saved,
                                                                plan
                                                        )
                                        );
                            });
                });
    }
    @Override
    public Flux<PolicyApplicationResponse> getMyApplications(
            AuthenticatedUser user) {

        return applicationRepository
                .findAllByFarmerUserIdOrderByCreatedAtDesc(
                        user.userId()
                )
                .flatMap(this::addPlanDetails);
    }

    @Override
    public Mono<PolicyApplicationResponse> getMyApplication(
            AuthenticatedUser user,
            Long applicationId) {

        return findOwnedApplication(
                user.userId(),
                applicationId
        ).flatMap(this::addPlanDetails);
    }

    @Override
    public Mono<PolicyApplicationResponse> updateApplication(
            AuthenticatedUser user,
            Long applicationId,
            PolicyApplicationRequest request) {

        return findOwnedApplication(
                user.userId(),
                applicationId
        )
                .flatMap(application -> {

                    if (!DRAFT.equals(application.getStatus())) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "APPLICATION_NOT_EDITABLE",
                                        "Only draft applications can be updated"
                                )
                        );
                    }

                    return validatePlanAndRequest(request)
                            .flatMap(plan -> {

                                application.setFarmId(
                                        request.farmId()
                                );

                                application.setCropId(
                                        request.cropId()
                                );

                                application.setPlanId(
                                        request.planId()
                                );

                                application.setRequestedCoverage(
                                        request.requestedCoverage()
                                );

                                application.setProposedStartDate(
                                        request.proposedStartDate()
                                );

                                application.setProposedEndDate(
                                        request.proposedEndDate()
                                );

                                application.setUpdatedAt(
                                        LocalDateTime.now()
                                );

                                return applicationRepository
                                        .save(application)
                                        .map(saved ->
                                                PolicyApplicationMapper
                                                        .toResponse(
                                                                saved,
                                                                plan
                                                        )
                                        );
                            });
                });
    }

    @Override
    public Mono<PolicyApplicationResponse> submitApplication(
            AuthenticatedUser user,
            Long applicationId) {

        return findOwnedApplication(
                user.userId(),
                applicationId
        )
                .flatMap(application -> {

                    if (!DRAFT.equals(application.getStatus())) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "APPLICATION_ALREADY_SUBMITTED",
                                        "Only draft applications can be submitted"
                                )
                        );
                    }

                    Mono<FarmApplicationValidationResponse>
                            farmValidation =
                            farmServiceClient.validateFarmAndCrop(
                                    user,
                                    application.getFarmId(),
                                    application.getCropId()
                            );

                    Mono<InsurancePlan> planMono =
                            planRepository
                                    .findById(
                                            application.getPlanId()
                                    )
                                    .switchIfEmpty(
                                            Mono.error(
                                                    new ApiException(
                                                            HttpStatus.NOT_FOUND,
                                                            "PLAN_NOT_FOUND",
                                                            "Insurance plan was not found"
                                                    )
                                            )
                                    );

                    return Mono.zip(
                                    farmValidation,
                                    planMono
                            )
                            .flatMap(validationResult -> {

                                FarmApplicationValidationResponse
                                        verifiedFarm =
                                        validationResult.getT1();

                                InsurancePlan plan =
                                        validationResult.getT2();

                                if (!verifiedFarm.eligible()) {
                                    return Mono.error(
                                            new ApiException(
                                                    HttpStatus.CONFLICT,
                                                    "FARM_NOT_ELIGIBLE",
                                                    "Farm is not eligible for submission"
                                            )
                                    );
                                }

                                if (!"ACTIVE".equals(
                                        plan.getStatus())) {

                                    return Mono.error(
                                            new ApiException(
                                                    HttpStatus.CONFLICT,
                                                    "PLAN_NOT_ACTIVE",
                                                    "Selected insurance plan is not active"
                                            )
                                    );
                                }

                                application.setStatus(SUBMITTED);

                                application.setSubmittedAt(
                                        LocalDateTime.now()
                                );

                                application.setUpdatedAt(
                                        LocalDateTime.now()
                                );

                                return applicationRepository
                                        .save(application)
                                        .map(saved ->
                                                PolicyApplicationMapper
                                                        .toResponse(
                                                                saved,
                                                                plan
                                                        )
                                        );
                            });
                });
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

    private Mono<InsurancePlan> validatePlanAndRequest(
            PolicyApplicationRequest request) {

        if (!request.proposedEndDate()
                .isAfter(request.proposedStartDate())) {

            return Mono.error(
                    new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "INVALID_POLICY_DATES",
                            "Proposed end date must be after proposed start date"
                    )
            );
        }

        return planRepository
                .findById(request.planId())
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "PLAN_NOT_FOUND",
                                        "Insurance plan was not found"
                                )
                        )
                )
                .flatMap(plan ->
                        validatePlanRules(plan, request)
                );
    }

    private Mono<InsurancePlan> validatePlanRules(
            InsurancePlan plan,
            PolicyApplicationRequest request) {

        if (!"ACTIVE".equals(plan.getStatus())) {
            return Mono.error(
                    new ApiException(
                            HttpStatus.CONFLICT,
                            "PLAN_NOT_ACTIVE",
                            "Selected insurance plan is not active"
                    )
            );
        }

        boolean belowMinimum =
                request.requestedCoverage()
                        .compareTo(plan.getMinimumCoverage()) < 0;

        boolean aboveMaximum =
                request.requestedCoverage()
                        .compareTo(plan.getMaximumCoverage()) > 0;

        if (belowMinimum || aboveMaximum) {
            return Mono.error(
                    new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "INVALID_COVERAGE_AMOUNT",
                            "Requested coverage is outside the plan limits"
                    )
            );
        }

        long policyDays =
                ChronoUnit.DAYS.between(
                        request.proposedStartDate(),
                        request.proposedEndDate()
                );

        if (policyDays > plan.getMaximumPolicyDays()) {
            return Mono.error(
                    new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "POLICY_PERIOD_EXCEEDED",
                            "Requested policy period exceeds the plan limit"
                    )
            );
        }

        return Mono.just(plan);
    }

    private Mono<PolicyApplicationResponse> addPlanDetails(
            PolicyApplication application) {

        return planRepository
                .findById(application.getPlanId())
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "PLAN_NOT_FOUND",
                                        "Insurance plan was not found"
                                )
                        )
                )
                .map(plan ->
                        PolicyApplicationMapper.toResponse(
                                application,
                                plan
                        )
                );
    }
}