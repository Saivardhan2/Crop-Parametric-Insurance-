package org.example.eventclaimsservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.eventclaimsservice.client.PolicyServiceClient;
import org.example.eventclaimsservice.dto.ClaimResponse;
import org.example.eventclaimsservice.dto.IrrigationReadingRequest;
import org.example.eventclaimsservice.dto.PayoutSlabConfig;
import org.example.eventclaimsservice.dto.PolicyClaimConfigurationResponse;
import org.example.eventclaimsservice.entity.Claim;
import org.example.eventclaimsservice.entity.IrrigationReading;
import org.example.eventclaimsservice.exception.ApiException;
import org.example.eventclaimsservice.repository.ClaimRepository;
import org.example.eventclaimsservice.repository.IrrigationReadingRepository;
import org.example.eventclaimsservice.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EventClaimsServiceImpl implements EventClaimsService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventClaimsServiceImpl.class);

    private static final BigDecimal DEFAULT_CLAIM_THRESHOLD =
            new BigDecimal("70.00");

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100.00");

    private final IrrigationReadingRepository readingRepository;
    private final ClaimRepository claimRepository;
    private final PolicyServiceClient policyServiceClient;

    public EventClaimsServiceImpl(
            IrrigationReadingRepository readingRepository,
            ClaimRepository claimRepository,
            PolicyServiceClient policyServiceClient) {

        this.readingRepository = readingRepository;
        this.claimRepository = claimRepository;
        this.policyServiceClient = policyServiceClient;
    }

    @Override
    public Mono<ClaimResponse> processReading(
            AuthenticatedUser dataProvider,
            IrrigationReadingRequest request) {

        LocalDateTime recordedAt =
                request.recordedAt() == null
                        ? LocalDateTime.now()
                        : request.recordedAt();

        if (recordedAt.isAfter(LocalDateTime.now())) {
            return Mono.error(
                    new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "FUTURE_READING_NOT_ALLOWED",
                            "Reading time cannot be in the future"
                    )
            );
        }

        return policyServiceClient
                .getPolicyClaimConfiguration(
                        dataProvider,
                        request.policyId()
                )
                .flatMap(config -> {
                    if (isOutsidePolicyPeriod(recordedAt, config)) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "READING_OUTSIDE_POLICY_PERIOD",
                                        "Reading date must be within the policy period"
                                )
                        );
                    }

                    IrrigationReading reading = createReading(
                            dataProvider,
                            request,
                            config,
                            recordedAt
                    );

                    return readingRepository
                            .save(reading)
                            .flatMap(savedReading ->
                                    evaluateReading(
                                            savedReading,
                                            config
                                    )
                            );
                });
    }

    @Override
    public Flux<ClaimResponse> getMyClaims(
            AuthenticatedUser farmer) {

        return claimRepository
                .findAllByFarmerUserIdOrderByCreatedAtDesc(
                        farmer.userId()
                )
                .map(this::toClaimResponse);
    }

    private IrrigationReading createReading(
            AuthenticatedUser dataProvider,
            IrrigationReadingRequest request,
            PolicyClaimConfigurationResponse policy,
            LocalDateTime recordedAt) {

        return IrrigationReading.builder()
                .policyId(policy.policyId())
                .farmId(policy.farmId())
                .availabilityPercentage(
                        request.availabilityPercentage()
                )
                .recordedAt(recordedAt)
                .submittedBy(dataProvider.userId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Mono<ClaimResponse> evaluateReading(
            IrrigationReading reading,
            PolicyClaimConfigurationResponse config) {

        BigDecimal availability = reading.getAvailabilityPercentage();

        // 1. Determine active trigger threshold (configured rule or fallback default)
        final BigDecimal threshold = (config.triggerRule() != null
                && config.triggerRule().availabilityThresholdPercentage() != null)
                ? config.triggerRule().availabilityThresholdPercentage()
                : DEFAULT_CLAIM_THRESHOLD;

        // 2. Normal reading - threshold not breached: no claim needed
        if (availability.compareTo(threshold) >= 0) {
            return Mono.just(createNoClaimResponse(reading));
        }

        // 3. Threshold breached: identify event date from reading
        LocalDate eventDate = reading.getRecordedAt().toLocalDate();

        // 4. Check whether a settled claim already exists for this (policyId, eventDate)
        return claimRepository
                .existsByPolicyIdAndEventDateAndStatus(
                        config.policyId(),
                        eventDate,
                        "SETTLED"
                )
                .flatMap(claimExists -> {
                    if (claimExists) {
                        return Mono.<ClaimResponse>error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "EVENT_CLAIM_ALREADY_EXISTS",
                                        "A claim has already been settled for this policy on " + eventDate
                                )
                        );
                    }

                    // 5. Determine payout percentage based on configured slabs (or default tiers)
                    BigDecimal payoutPercentage = determinePayoutPercentage(
                            availability,
                            threshold,
                            config.payoutSlabs()
                    );

                    // 6. Calculate base compensation = coverageAmount * payoutPercentage / 100
                    BigDecimal calculatedCompensation = calculateCompensation(
                            config.coverageAmount(),
                            payoutPercentage
                    );

                    // 7. Aggregate coverage limit check: calculate remaining coverage
                    return claimRepository
                            .findAllByPolicyIdAndStatus(config.policyId(), "SETTLED")
                            .map(Claim::getCompensationAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .flatMap(totalSettledSoFar -> {
                                BigDecimal remainingCoverage =
                                        config.coverageAmount().subtract(totalSettledSoFar);

                                if (remainingCoverage.compareTo(BigDecimal.ZERO) <= 0) {
                                    return Mono.<ClaimResponse>error(
                                            new ApiException(
                                                    HttpStatus.BAD_REQUEST,
                                                    "COVERAGE_EXHAUSTED",
                                                    "Total policy coverage has already been fully paid out"
                                            )
                                    );
                                }

                                // Cap compensation to remaining coverage
                                BigDecimal actualCompensation =
                                        calculatedCompensation.min(remainingCoverage);

                                Claim claim = createClaim(
                                        reading,
                                        config,
                                        eventDate,
                                        payoutPercentage,
                                        actualCompensation
                                );

                                return claimRepository
                                        .save(claim)
                                        .map(this::toClaimResponse);
                            });
                });
    }

    private BigDecimal determinePayoutPercentage(
            BigDecimal availability,
            BigDecimal threshold,
            List<PayoutSlabConfig> slabs) {

        if (slabs != null && !slabs.isEmpty()) {
            for (PayoutSlabConfig slab : slabs) {
                if (slab.minAvailabilityPct() != null && slab.maxAvailabilityPct() != null) {
                    if (availability.compareTo(slab.minAvailabilityPct()) >= 0
                            && availability.compareTo(slab.maxAvailabilityPct()) < 0) {
                        return slab.payoutPercentage();
                    }
                }
            }
        }

        // Default severity-based tiers
        if (availability.compareTo(new BigDecimal("30.00")) < 0) {
            return new BigDecimal("50.00");
        }
        if (availability.compareTo(new BigDecimal("50.00")) < 0) {
            return new BigDecimal("25.00");
        }
        return new BigDecimal("10.00");
    }

    private BigDecimal calculateCompensation(
            BigDecimal coverageAmount,
            BigDecimal payoutPercentage) {

        return coverageAmount
                .multiply(payoutPercentage)
                .divide(
                        ONE_HUNDRED,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private Claim createClaim(
            IrrigationReading reading,
            PolicyClaimConfigurationResponse policy,
            LocalDate eventDate,
            BigDecimal payoutPercentage,
            BigDecimal compensationAmount) {

        LocalDateTime now = LocalDateTime.now();

        return Claim.builder()
                .readingId(reading.getReadingId())
                .policyId(policy.policyId())
                .eventDate(eventDate)
                .farmerUserId(policy.farmerUserId())
                .farmId(policy.farmId())
                .availabilityPercentage(
                        reading.getAvailabilityPercentage()
                )
                .payoutPercentage(payoutPercentage)
                .coverageAmount(policy.coverageAmount())
                .compensationAmount(compensationAmount)
                .payoutStatus("SUCCESS")
                .status("SETTLED")
                .createdAt(now)
                .settledAt(now)
                .build();
    }

    private ClaimResponse createNoClaimResponse(
            IrrigationReading reading) {

        return new ClaimResponse(
                reading.getReadingId(),
                null,
                reading.getPolicyId(),
                reading.getFarmId(),
                reading.getAvailabilityPercentage(),
                false,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "NOT_REQUIRED",
                "NO_CLAIM",
                reading.getRecordedAt()
        );
    }

    private ClaimResponse toClaimResponse(
            Claim claim) {

        return new ClaimResponse(
                claim.getReadingId(),
                claim.getClaimId(),
                claim.getPolicyId(),
                claim.getFarmId(),
                claim.getAvailabilityPercentage(),
                true,
                claim.getPayoutPercentage(),
                claim.getCompensationAmount(),
                claim.getPayoutStatus(),
                claim.getStatus(),
                claim.getCreatedAt()
        );
    }

    private boolean isOutsidePolicyPeriod(
            LocalDateTime recordedAt,
            PolicyClaimConfigurationResponse policy) {

        return recordedAt.toLocalDate()
                .isBefore(policy.startDate())
                || recordedAt.toLocalDate()
                .isAfter(policy.endDate());
    }
}
