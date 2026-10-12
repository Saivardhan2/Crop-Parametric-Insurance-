package org.example.policyservice.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.example.policyservice.dto.request
        .TriggerRuleRequest;
import org.example.policyservice.dto.response
        .TriggerRuleResponse;
import org.example.policyservice.entity.TriggerRule;
import org.example.policyservice.exception.ApiException;
import org.example.policyservice.repository
        .InsurancePlanRepository;
import org.example.policyservice.repository
        .TriggerRuleRepository;
import org.example.policyservice.util.TriggerRuleMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TriggerRuleServiceImpl
        implements TriggerRuleService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TriggerRuleServiceImpl.class);

    private final TriggerRuleRepository ruleRepository;
    private final InsurancePlanRepository planRepository;

    public TriggerRuleServiceImpl(
            TriggerRuleRepository ruleRepository,
            InsurancePlanRepository planRepository) {

        this.ruleRepository = ruleRepository;
        this.planRepository = planRepository;
    }

    @Override
    public Mono<TriggerRuleResponse> createRule(
            Long planId,
            TriggerRuleRequest request) {

        if (request.requiredShortageDays()
                > request.rollingWindowDays()) {

            return Mono.error(
                    new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "INVALID_SHORTAGE_WINDOW",
                            "Required shortage days cannot exceed rolling window days"
                    )
            );
        }

        String cropType = normalize(
                request.cropType()
        );

        String growthStage = normalize(
                request.growthStage()
        );

        return planRepository.findById(planId)
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
                        ruleRepository
                                .existsByPlanIdAndCropTypeIgnoreCaseAndGrowthStageIgnoreCase(
                                        planId,
                                        cropType,
                                        growthStage
                                )
                )
                .flatMap(exists -> {

                    if (exists) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "TRIGGER_RULE_EXISTS",
                                        "A trigger rule already exists for this crop and growth stage"
                                )
                        );
                    }

                    LocalDateTime now =
                            LocalDateTime.now();

                    TriggerRule rule =
                            TriggerRule.builder()
                                    .planId(planId)
                                    .cropType(cropType)
                                    .growthStage(growthStage)
                                    .availabilityThresholdPercentage(
                                            request
                                                    .availabilityThresholdPercentage()
                                    )
                                    .minimumContinuousHours(
                                            request
                                                    .minimumContinuousHours()
                                    )
                                    .rollingWindowDays(
                                            request
                                                    .rollingWindowDays()
                                    )
                                    .requiredShortageDays(
                                            request
                                                    .requiredShortageDays()
                                    )
                                    .status("ACTIVE")
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build();

                    return ruleRepository.save(rule);
                })
                .map(TriggerRuleMapper::toResponse);
    }

    @Override
    public Flux<TriggerRuleResponse>
    getRulesForPlan(Long planId) {

        return planRepository.findById(planId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "PLAN_NOT_FOUND",
                                        "Insurance plan was not found"
                                )
                        )
                )
                .flatMapMany(plan ->
                        ruleRepository
                                .findAllByPlanIdAndStatusOrderByCreatedAtDesc(
                                        planId,
                                        "ACTIVE"
                                )
                )
                .map(TriggerRuleMapper::toResponse);
    }

    private String normalize(String value) {

        return value.trim()
                .toUpperCase(Locale.ROOT);
    }
}