package org.example.eventclaimsservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.eventclaimsservice.client.PolicyServiceClient;
import org.example.eventclaimsservice.dto.ClaimDecisionRequest;
import org.example.eventclaimsservice.dto.ClaimFeedbackRequest;
import org.example.eventclaimsservice.dto.IrrigationReadingRequest;
import org.example.eventclaimsservice.dto.PolicyClaimConfigurationResponse;
import org.example.eventclaimsservice.repository.ClaimRepository;
import org.example.eventclaimsservice.repository.IrrigationReadingRepository;
import org.example.eventclaimsservice.security.AuthenticatedUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class EventClaimsMicroserviceIntegrationTest {

    private static final String READINGS_URL =
            "/api/readings";

    private static final String DATA_PROVIDER_ID =
            "5";

    private static final String DATA_PROVIDER_EMAIL =
            "provider@irrigashield.com";

    private static final String DATA_PROVIDER_ROLE =
            "ROLE_DATA_PROVIDER";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private IrrigationReadingRepository readingRepository;

    @MockitoBean
    private PolicyServiceClient policyServiceClient;

    private PolicyClaimConfigurationResponse activePolicyConfig;

    @BeforeEach
    void setUp() {
        claimRepository
                .deleteAll()
                .then(readingRepository.deleteAll())
                .block();

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
    void normalReadingShouldReturnNoClaim() {

        mockActivePolicy();

        IrrigationReadingRequest request =
                createRequest("80.00");

        dataProviderPost(request)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.readingId")
                .isNumber()
                .jsonPath("$.claimId")
                .doesNotExist()
                .jsonPath("$.policyId")
                .isEqualTo(1)
                .jsonPath("$.farmId")
                .isEqualTo(1)
                .jsonPath("$.availabilityPercentage")
                .isEqualTo(80.0)
                .jsonPath("$.thresholdBreached")
                .isEqualTo(false)
                .jsonPath("$.payoutPercentage")
                .isEqualTo(0)
                .jsonPath("$.compensationAmount")
                .isEqualTo(0)
                .jsonPath("$.payoutStatus")
                .isEqualTo("NOT_REQUIRED")
                .jsonPath("$.claimStatus")
                .isEqualTo("NO_CLAIM");
    }

    @Test
    void lowReadingShouldCreatePendingApprovalClaimAndOfficerApprovalShouldSettleIt() {

        mockActivePolicy();

        IrrigationReadingRequest request =
                createRequest("40.00");

        // 1. Reading creates claim in PENDING_APPROVAL
        byte[] responseBytes = dataProviderPost(request)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.readingId")
                .isNumber()
                .jsonPath("$.claimId")
                .isNumber()
                .jsonPath("$.policyId")
                .isEqualTo(1)
                .jsonPath("$.farmId")
                .isEqualTo(1)
                .jsonPath("$.availabilityPercentage")
                .isEqualTo(40.0)
                .jsonPath("$.thresholdBreached")
                .isEqualTo(true)
                .jsonPath("$.payoutPercentage")
                .isEqualTo(25.0)
                .jsonPath("$.compensationAmount")
                .isEqualTo(15000.0)
                .jsonPath("$.payoutStatus")
                .isEqualTo("PENDING")
                .jsonPath("$.claimStatus")
                .isEqualTo("PENDING_APPROVAL")
                .returnResult()
                .getResponseBody();

        Long claimId = claimRepository.findAll().blockFirst().getClaimId();

        // 2. Claims Officer approves the pending claim -> Claim is SETTLED
        webTestClient.put()
                .uri("/api/claims/" + claimId + "/approve")
                .header("X-User-Id", "99")
                .header("X-User-Email", "officer@irrigashield.com")
                .header("X-User-Roles", "ROLE_CLAIMS_OFFICER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClaimDecisionRequest("Approved by officer after sensor verification"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.claimId")
                .isEqualTo(claimId)
                .jsonPath("$.claimStatus")
                .isEqualTo("SETTLED")
                .jsonPath("$.payoutStatus")
                .isEqualTo("SUCCESS")
                .jsonPath("$.claimsOfficerRemarks")
                .isEqualTo("Approved by officer after sensor verification");

        // 3. Farmer submits feedback on settled claim
        webTestClient.post()
                .uri("/api/claims/" + claimId + "/feedback")
                .header("X-User-Id", "2") // Farmer user ID from policy config
                .header("X-User-Email", "farmer@irrigashield.com")
                .header("X-User-Roles", "ROLE_FARMER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClaimFeedbackRequest("SATISFIED", 5, "Prompt settlement", BigDecimal.ZERO))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.feedbackStatus")
                .isEqualTo("SATISFIED")
                .jsonPath("$.feedbackRating")
                .isEqualTo(5);
    }

    @Test
    void duplicateSettledClaimShouldReturnConflict() {

        mockActivePolicy();

        dataProviderPost(createRequest("40.00"))
                .exchange()
                .expectStatus()
                .isCreated();

        dataProviderPost(createRequest("25.00"))
                .exchange()
                .expectStatus()
                .isEqualTo(409)
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo(409)
                .jsonPath("$.errorCode")
                .isEqualTo(
                        "EVENT_CLAIM_ALREADY_EXISTS"
                )
                .jsonPath("$.message")
                .isNotEmpty()
                .jsonPath("$.path")
                .isEqualTo("/api/readings");
    }

    @Test
    void farmerShouldNotSubmitReading() {

        IrrigationReadingRequest request =
                createRequest("80.00");

        webTestClient
                .post()
                .uri(READINGS_URL)
                .header("X-User-Id", "2")
                .header(
                        "X-User-Email",
                        "farmer@example.com"
                )
                .header(
                        "X-User-Roles",
                        "ROLE_FARMER"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void unauthenticatedRequestShouldReturnUnauthorized() {

        IrrigationReadingRequest request =
                createRequest("80.00");

        webTestClient
                .post()
                .uri(READINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    private WebTestClient.RequestHeadersSpec<?> dataProviderPost(
            IrrigationReadingRequest request) {

        return webTestClient
                .post()
                .uri(READINGS_URL)
                .header(
                        "X-User-Id",
                        DATA_PROVIDER_ID
                )
                .header(
                        "X-User-Email",
                        DATA_PROVIDER_EMAIL
                )
                .header(
                        "X-User-Roles",
                        DATA_PROVIDER_ROLE
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request);
    }

    private IrrigationReadingRequest createRequest(String availability) {
        return new IrrigationReadingRequest(1L,
                new BigDecimal(availability),
                LocalDateTime.now().minusDays(1)
        );
    }

    private void mockActivePolicy() {
        when(policyServiceClient.getPolicyClaimConfiguration(
                        any(AuthenticatedUser.class),
                        eq(1L)))
                .thenReturn(
                        Mono.just(activePolicyConfig)
                );
    }
}