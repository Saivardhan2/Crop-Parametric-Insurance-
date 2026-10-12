package org.example.eventclaimsservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.eventclaimsservice.client.PolicyServiceClient;
import org.example.eventclaimsservice.dto.IrrigationReadingRequest;
import org.example.eventclaimsservice.dto.PolicyClaimConfigurationResponse;
import org.example.eventclaimsservice.entity.Claim;
import org.example.eventclaimsservice.entity.IrrigationReading;
import org.example.eventclaimsservice.exception.ApiException;
import org.example.eventclaimsservice.repository.ClaimRepository;
import org.example.eventclaimsservice.repository.IrrigationReadingRepository;
import org.example.eventclaimsservice.security.AuthenticatedUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class EventClaimsServiceImplTest {

    @Mock
    private IrrigationReadingRepository readingRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyServiceClient policyServiceClient;

    private EventClaimsServiceImpl service;
    private AuthenticatedUser dataProvider;
    private PolicyClaimConfigurationResponse activePolicyConfig;

    @BeforeEach
    void setUp() {

        service = new EventClaimsServiceImpl(
                readingRepository,
                claimRepository,
                policyServiceClient
        );

        dataProvider = new AuthenticatedUser(
                5L,
                "provider@irrigashield.com"
        );

        activePolicyConfig = new PolicyClaimConfigurationResponse(
                1L,
                2L,
                1L,
                1L,
                1L,
                new BigDecimal("60000.00"),
                LocalDate.now().minusDays(30),
                LocalDate.now().plusDays(30),
                "ACTIVE",
                null,
                List.of()
        );
    }

    @Test
    void normalReadingShouldNotCreateClaim() {

        IrrigationReadingRequest request =
                createRequest("80.00");

        mockActivePolicy();
        mockReadingSave(1L);

        StepVerifier.create(
                        service.processReading(
                                dataProvider,
                                request
                        )
                )
                .assertNext(response -> {
                    assertEquals(1L, response.readingId());
                    assertNull(response.claimId());
                    assertFalse(response.thresholdBreached());
                    assertEquals(
                            "NO_CLAIM",
                            response.claimStatus()
                    );
                    assertEquals(
                            "NOT_REQUIRED",
                            response.payoutStatus()
                    );
                })
                .verifyComplete();

        verify(claimRepository, never())
                .save(any(Claim.class));
    }

    @Test
    void lowReadingShouldCreateSettledClaim() {

        IrrigationReadingRequest request =
                createRequest("40.00");

        mockActivePolicy();
        mockReadingSave(2L);

        when(claimRepository.existsByPolicyIdAndEventDateAndStatus(
                eq(1L),
                any(LocalDate.class),
                eq("SETTLED")
        )).thenReturn(Mono.just(false));

        when(claimRepository.findAllByPolicyIdAndStatus(1L, "SETTLED"))
                .thenReturn(Flux.empty());

        mockClaimSave(1L);

        StepVerifier.create(
                        service.processReading(
                                dataProvider,
                                request
                        )
                )
                .assertNext(response -> {
                    assertEquals(1L, response.claimId());
                    assertTrue(response.thresholdBreached());

                    assertEquals(
                            new BigDecimal("25.00"),
                            response.payoutPercentage()
                    );

                    assertEquals(
                            new BigDecimal("15000.00"),
                            response.compensationAmount()
                    );

                    assertEquals(
                            "SUCCESS",
                            response.payoutStatus()
                    );

                    assertEquals(
                            "SETTLED",
                            response.claimStatus()
                    );
                })
                .verifyComplete();

        verify(claimRepository)
                .save(any(Claim.class));
    }

    @Test
    void duplicateSettledClaimForSameEventShouldBeRejected() {

        IrrigationReadingRequest request =
                createRequest("25.00");

        mockActivePolicy();
        mockReadingSave(3L);

        when(claimRepository.existsByPolicyIdAndEventDateAndStatus(
                eq(1L),
                any(LocalDate.class),
                eq("SETTLED")
        )).thenReturn(Mono.just(true));

        StepVerifier.create(
                        service.processReading(
                                dataProvider,
                                request
                        )
                )
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof ApiException);

                    ApiException exception =
                            (ApiException) error;

                    assertEquals(
                            "EVENT_CLAIM_ALREADY_EXISTS",
                            exception.getErrorCode()
                    );
                })
                .verify();

        verify(claimRepository, never())
                .save(any(Claim.class));
    }

    @Test
    void futureReadingShouldBeRejected() {

        IrrigationReadingRequest request =
                new IrrigationReadingRequest(
                        1L,
                        new BigDecimal("40.00"),
                        LocalDateTime.now().plusDays(1)
                );

        StepVerifier.create(
                        service.processReading(
                                dataProvider,
                                request
                        )
                )
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof ApiException);

                    ApiException exception =
                            (ApiException) error;

                    assertEquals(
                            "FUTURE_READING_NOT_ALLOWED",
                            exception.getErrorCode()
                    );
                })
                .verify();

        verify(readingRepository, never())
                .save(any(IrrigationReading.class));

        verify(claimRepository, never())
                .save(any(Claim.class));
    }

    private IrrigationReadingRequest createRequest(
            String availability) {

        return new IrrigationReadingRequest(
                1L,
                new BigDecimal(availability),
                LocalDateTime.now().minusDays(1)
        );
    }

    private void mockActivePolicy() {

        when(policyServiceClient.getPolicyClaimConfiguration(
                dataProvider,
                1L
        )).thenReturn(Mono.just(activePolicyConfig));
    }

    private void mockReadingSave(Long readingId) {

        when(readingRepository.save(
                any(IrrigationReading.class)
        )).thenAnswer(invocation -> {

            IrrigationReading reading =
                    invocation.getArgument(0);

            reading.setReadingId(readingId);

            return Mono.just(reading);
        });
    }

    private void mockClaimSave(Long claimId) {

        when(claimRepository.save(
                any(Claim.class)
        )).thenAnswer(invocation -> {

            Claim claim =
                    invocation.getArgument(0);

            claim.setClaimId(claimId);

            return Mono.just(claim);
        });
    }
}
