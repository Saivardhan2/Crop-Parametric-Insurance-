package org.example.farmservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.example.farmservice.config.SecurityConfig;
import org.example.farmservice.dto.request.FarmRequest;
import org.example.farmservice.dto.response.FarmResponse;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.security.TrustedHeaderAuthenticationFilter;
import org.example.farmservice.service.FarmService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(FarmController.class)
@Import({
        SecurityConfig.class,
        TrustedHeaderAuthenticationFilter.class
})
class FarmControllerIntegrationTest {

    private static final String FARMER_ID = "2";
    private static final String FARMER_EMAIL =
            "farmer@example.com";
    private static final String FARMER_ROLE =
            "ROLE_FARMER";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FarmService farmService;

    private FarmResponse farmResponse;

    @BeforeEach
    void setUp() {

        farmResponse = mock(FarmResponse.class);
    }

    @Test
    void farmerShouldCreateFarm() {

        when(farmService.createFarm(
                any(AuthenticatedUser.class),
                any(FarmRequest.class)
        )).thenReturn(Mono.just(farmResponse));

        farmerPost("/api/farms")
                .bodyValue(validFarmRequest())
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void farmerShouldGetOwnFarms() {

        when(farmService.getMyFarms(
                any(AuthenticatedUser.class)
        )).thenReturn(Flux.just(farmResponse));

        farmerGet("/api/farms/my-farms")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$")
                .isArray();
    }

    @Test
    void underwriterShouldNotAccessFarmEndpoint() {

        webTestClient
                .get()
                .uri("/api/farms/my-farms")
                .header("X-User-Id", "3")
                .header(
                        "X-User-Email",
                        "underwriter@irrigashield.com"
                )
                .header(
                        "X-User-Roles",
                        "ROLE_UNDERWRITER"
                )
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void unauthenticatedRequestShouldReturnUnauthorized() {

        webTestClient
                .get()
                .uri("/api/farms/my-farms")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    private WebTestClient.RequestBodySpec farmerPost(
            String endpoint) {

        return webTestClient
                .post()
                .uri(endpoint)
                .header(
                        "X-User-Id",
                        FARMER_ID
                )
                .header(
                        "X-User-Email",
                        FARMER_EMAIL
                )
                .header(
                        "X-User-Roles",
                        FARMER_ROLE
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                );
    }

    private WebTestClient.RequestHeadersSpec<?> farmerGet(
            String endpoint) {

        return webTestClient
                .get()
                .uri(endpoint)
                .header(
                        "X-User-Id",
                        FARMER_ID
                )
                .header(
                        "X-User-Email",
                        FARMER_EMAIL
                )
                .header(
                        "X-User-Roles",
                        FARMER_ROLE
                );
    }

    private String validFarmRequest() {

        return """
                {
                  "farmName": "Green Farm",
                  "surveyNumber": "SURVEY-101",
                  "addressLine": "Main Road",
                  "village": "Green Village",
                  "district": "Pune",
                  "state": "Maharashtra",
                  "postalCode": "411001",
                  "areaInAcres": 5.50,
                  "soilType": "LOAMY",
                  "ownershipType": "OWNED",
                  "latitude": 18.520400,
                  "longitude": 73.856700
                }
                """;
    }
}