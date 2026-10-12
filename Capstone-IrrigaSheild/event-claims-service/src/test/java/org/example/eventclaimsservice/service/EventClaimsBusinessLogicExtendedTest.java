package org.example.eventclaimsservice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.eventclaimsservice.client.PolicyServiceClient;
import org.example.eventclaimsservice.dto.IrrigationReadingRequest;
import org.example.eventclaimsservice.dto.PolicyClaimConfigurationResponse;
import org.example.eventclaimsservice.dto.TriggerRuleConfig;
import org.example.eventclaimsservice.entity.Claim;
import org.example.eventclaimsservice.entity.IrrigationReading;
import org.example.eventclaimsservice.exception.ApiException;
import org.example.eventclaimsservice.repository.ClaimRepository;
import org.example.eventclaimsservice.repository.IrrigationReadingRepository;
import org.example.eventclaimsservice.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventClaimsBusinessLogicExtendedTest {

    @Mock
    private IrrigationReadingRepository readingRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyServiceClient policyServiceClient;

    private EventClaimsServiceImpl service;
    private final AuthenticatedUser provider = new AuthenticatedUser(99L, "provider@test.com");

    private PolicyClaimConfigurationResponse sampleConfig;

    @BeforeEach
    void setUp() {
        service = new EventClaimsServiceImpl(readingRepository, claimRepository, policyServiceClient);

        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().plusDays(30);

        TriggerRuleConfig triggerRule = new TriggerRuleConfig(
                1L, 100L, "WHEAT", "VEGETATIVE",
                new BigDecimal("50.00"), 12, 7, 3, "ACTIVE"
        );

        sampleConfig = new PolicyClaimConfigurationResponse(
                10L, 5L, 2L, 3L, 100L,
                new BigDecimal("50000.00"), startDate, endDate, "ACTIVE",
                triggerRule, List.of()
        );
    }

    @Test
    @DisplayName("processReading should reject reading with future timestamp")
    void testFutureReadingRejected() {
        IrrigationReadingRequest req = new IrrigationReadingRequest(
                10L,
                new BigDecimal("40.00"),
                LocalDateTime.now().plusDays(2)
        );

        StepVerifier.create(service.processReading(provider, req))
                .expectErrorMatches(ex -> ex instanceof ApiException
                        && ((ApiException) ex).getStatus() == HttpStatus.BAD_REQUEST
                        && ((ApiException) ex).getErrorCode().equals("FUTURE_READING_NOT_ALLOWED"))
                .verify();
    }

    @Test
    @DisplayName("processReading should reject reading before policy start date")
    void testReadingBeforePolicyPeriod() {
        LocalDateTime pastDate = sampleConfig.startDate().minusDays(5).atStartOfDay();
        IrrigationReadingRequest req = new IrrigationReadingRequest(
                10L,
                new BigDecimal("40.00"),
                pastDate
        );

        when(policyServiceClient.getPolicyClaimConfiguration(provider, 10L))
                .thenReturn(Mono.just(sampleConfig));

        StepVerifier.create(service.processReading(provider, req))
                .expectErrorMatches(ex -> ex instanceof ApiException
                        && ((ApiException) ex).getStatus() == HttpStatus.BAD_REQUEST
                        && ((ApiException) ex).getErrorCode().equals("READING_OUTSIDE_POLICY_PERIOD"))
                .verify();
    }

    @Test
    @DisplayName("processReading should reject reading after policy end date")
    void testReadingAfterPolicyPeriod() {
        PolicyClaimConfigurationResponse pastConfig = new PolicyClaimConfigurationResponse(
                10L, 5L, 2L, 3L, 100L,
                new BigDecimal("50000.00"),
                LocalDate.now().minusDays(60),
                LocalDate.now().minusDays(10),
                "ACTIVE",
                null, List.of()
        );

        LocalDateTime readingTime = LocalDate.now().minusDays(5).atStartOfDay();
        IrrigationReadingRequest req = new IrrigationReadingRequest(
                10L,
                new BigDecimal("40.00"),
                readingTime
        );

        when(policyServiceClient.getPolicyClaimConfiguration(provider, 10L))
                .thenReturn(Mono.just(pastConfig));

        StepVerifier.create(service.processReading(provider, req))
                .expectErrorMatches(ex -> ex instanceof ApiException
                        && ((ApiException) ex).getStatus() == HttpStatus.BAD_REQUEST
                        && ((ApiException) ex).getErrorCode().equals("READING_OUTSIDE_POLICY_PERIOD"))
                .verify();
    }

    @Test
    @DisplayName("processReading should accept reading with null recordedAt by setting current timestamp")
    void testNullRecordedAtUsesCurrentTime() {
        IrrigationReadingRequest req = new IrrigationReadingRequest(
                10L,
                new BigDecimal("80.00"),
                null
        );

        when(policyServiceClient.getPolicyClaimConfiguration(provider, 10L))
                .thenReturn(Mono.just(sampleConfig));

        IrrigationReading savedReading = IrrigationReading.builder()
                .readingId(101L)
                .policyId(10L)
                .farmId(2L)
                .availabilityPercentage(new BigDecimal("80.00"))
                .recordedAt(LocalDateTime.now())
                .submittedBy(99L)
                .build();

        when(readingRepository.save(any(IrrigationReading.class))).thenReturn(Mono.just(savedReading));

        StepVerifier.create(service.processReading(provider, req))
                .assertNext(res -> {
                    assertThat(res.readingId()).isEqualTo(101L);
                    assertThat(res.thresholdBreached()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getMyClaims should return empty Flux when farmer has no claims")
    void testGetMyClaimsEmpty() {
        AuthenticatedUser farmer = new AuthenticatedUser(55L, "farmer55@test.com");
        when(claimRepository.findAllByFarmerUserIdOrderByCreatedAtDesc(55L)).thenReturn(Flux.empty());

        StepVerifier.create(service.getMyClaims(farmer))
                .verifyComplete();
    }

    @Test
    @DisplayName("getMyClaims should stream mapped ClaimResponses when claims exist")
    void testGetMyClaimsPopulated() {
        AuthenticatedUser farmer = new AuthenticatedUser(55L, "farmer55@test.com");

        Claim claim = Claim.builder()
                .claimId(1L)
                .readingId(10L)
                .policyId(20L)
                .farmId(30L)
                .availabilityPercentage(new BigDecimal("20.00"))
                .payoutPercentage(new BigDecimal("50.00"))
                .compensationAmount(new BigDecimal("10000.00"))
                .payoutStatus("PAID")
                .status("APPROVED")
                .createdAt(LocalDateTime.now())
                .build();

        when(claimRepository.findAllByFarmerUserIdOrderByCreatedAtDesc(55L)).thenReturn(Flux.just(claim));

        StepVerifier.create(service.getMyClaims(farmer))
                .assertNext(res -> {
                    assertThat(res.claimId()).isEqualTo(1L);
                    assertThat(res.claimStatus()).isEqualTo("APPROVED");
                    assertThat(res.compensationAmount()).isEqualByComparingTo("10000.00");
                })
                .verifyComplete();
    }
}
