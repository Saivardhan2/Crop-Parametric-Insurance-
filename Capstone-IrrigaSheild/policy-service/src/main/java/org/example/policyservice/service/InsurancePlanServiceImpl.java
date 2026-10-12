package org.example.policyservice.service;

import java.time.LocalDateTime;

import org.example.policyservice.dto.request
        .InsurancePlanRequest;
import org.example.policyservice.dto.response
        .InsurancePlanResponse;
import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.exception.ApiException;
import org.example.policyservice.repository
        .InsurancePlanRepository;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.util.InsurancePlanMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class InsurancePlanServiceImpl
        implements InsurancePlanService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InsurancePlanServiceImpl.class);

    private final InsurancePlanRepository repository;

    public InsurancePlanServiceImpl(
            InsurancePlanRepository repository) {

        this.repository = repository;
    }

    @Override
    public Mono<InsurancePlanResponse> createPlan(
            AuthenticatedUser user,
            InsurancePlanRequest request) {

        if (request.maximumCoverage()
                .compareTo(request.minimumCoverage()) < 0) {

            return Mono.error(
                    new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "INVALID_COVERAGE_RANGE",
                            "Maximum coverage cannot be below minimum coverage"
                    )
            );
        }

        return repository
                .existsByPlanNameIgnoreCase(
                        request.planName().trim()
                )
                .flatMap(exists -> {

                    if (exists) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "PLAN_ALREADY_EXISTS",
                                        "An insurance plan with this name already exists"
                                )
                        );
                    }

                    LocalDateTime now =
                            LocalDateTime.now();

                    InsurancePlan plan =
                            InsurancePlan.builder()
                                    .planName(
                                            request.planName().trim()
                                    )
                                    .description(
                                            request.description().trim()
                                    )
                                    .minimumCoverage(
                                            request.minimumCoverage()
                                    )
                                    .maximumCoverage(
                                            request.maximumCoverage()
                                    )
                                    .basePremium(
                                            request.basePremium()
                                    )
                                    .maximumPolicyDays(
                                            request.maximumPolicyDays()
                                    )
                                    .status("ACTIVE")
                                    .createdBy(user.userId())
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build();

                    return repository.save(plan);
                })
                .map(InsurancePlanMapper::toResponse);
    }

    @Override
    public Flux<InsurancePlanResponse> getActivePlans() {

        return repository
                .findAllByStatusOrderByCreatedAtDesc(
                        "ACTIVE"
                )
                .map(InsurancePlanMapper::toResponse);
    }

    @Override
    public Flux<InsurancePlanResponse> getAllPlans() {

        return repository
                .findAllByOrderByCreatedAtDesc()
                .map(InsurancePlanMapper::toResponse);
    }

    @Override
    public Mono<InsurancePlanResponse> getPlan(
            Long planId) {

        return repository.findById(planId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "PLAN_NOT_FOUND",
                                        "Insurance plan was not found"
                                )
                        )
                )
                .map(InsurancePlanMapper::toResponse);
    }
}