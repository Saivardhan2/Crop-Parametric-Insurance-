package org.example.policyservice.service;

import java.time.LocalDateTime;

import org.example.policyservice.dto.request.PayoutSlabRequest;
import org.example.policyservice.dto.response.PayoutSlabResponse;
import org.example.policyservice.entity.PayoutSlab;
import org.example.policyservice.exception.ApiException;
import org.example.policyservice.repository.PayoutSlabRepository;
import org.example.policyservice.repository.TriggerRuleRepository;
import org.example.policyservice.util.PayoutSlabMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PayoutSlabServiceImpl implements PayoutSlabService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PayoutSlabServiceImpl.class);

    private final PayoutSlabRepository slabRepository;
    private final TriggerRuleRepository ruleRepository;

    public PayoutSlabServiceImpl(
            PayoutSlabRepository slabRepository,
            TriggerRuleRepository ruleRepository) {

        this.slabRepository = slabRepository;
        this.ruleRepository = ruleRepository;
    }

    @Override
    public Mono<PayoutSlabResponse> createSlab(
            Long triggerRuleId,
            PayoutSlabRequest request) {

        if (request.maximumDurationHours() != null
                && request.minimumDurationHours() != null
                && request.maximumDurationHours() < request.minimumDurationHours()) {

            return Mono.error(
                    new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "INVALID_DURATION_RANGE",
                            "Maximum duration cannot be below minimum duration"
                    )
            );
        }

        if (request.minAvailabilityPct() != null
                && request.maxAvailabilityPct() != null
                && request.maxAvailabilityPct().compareTo(request.minAvailabilityPct()) < 0) {

            return Mono.error(
                    new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "INVALID_AVAILABILITY_RANGE",
                            "Maximum availability cannot be below minimum availability"
                    )
            );
        }

        return ruleRepository.findById(triggerRuleId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "TRIGGER_RULE_NOT_FOUND",
                                        "Trigger rule was not found"
                                )
                        )
                )
                .flatMap(rule -> {
                    if (request.minimumDurationHours() != null) {
                        return slabRepository.existsByTriggerRuleIdAndMinimumDurationHours(
                                triggerRuleId,
                                request.minimumDurationHours()
                        );
                    }
                    return Mono.just(false);
                })
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "PAYOUT_SLAB_EXISTS",
                                        "A payout slab already exists for this minimum duration"
                                )
                        );
                    }

                    LocalDateTime now = LocalDateTime.now();

                    PayoutSlab slab = PayoutSlab.builder()
                            .triggerRuleId(triggerRuleId)
                            .minimumDurationHours(
                                    request.minimumDurationHours() != null
                                            ? request.minimumDurationHours()
                                            : 0
                            )
                            .maximumDurationHours(request.maximumDurationHours())
                            .minAvailabilityPct(request.minAvailabilityPct())
                            .maxAvailabilityPct(request.maxAvailabilityPct())
                            .payoutPercentage(request.payoutPercentage())
                            .status("ACTIVE")
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    return slabRepository.save(slab);
                })
                .map(PayoutSlabMapper::toResponse);
    }

    @Override
    public Flux<PayoutSlabResponse> getSlabs(Long triggerRuleId) {
        return ruleRepository.findById(triggerRuleId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "TRIGGER_RULE_NOT_FOUND",
                                        "Trigger rule was not found"
                                )
                        )
                )
                .flatMapMany(rule ->
                        slabRepository.findAllByTriggerRuleIdAndStatusOrderByMinimumDurationHoursAsc(
                                triggerRuleId,
                                "ACTIVE"
                        )
                )
                .map(PayoutSlabMapper::toResponse);
    }
}
