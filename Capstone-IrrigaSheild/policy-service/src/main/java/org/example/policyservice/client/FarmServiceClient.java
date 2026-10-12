package org.example.policyservice.client;

import org.example.policyservice.dto.response
        .FarmApplicationValidationResponse;
import org.example.policyservice.exception.ApiException;
import org.example.policyservice.security.AuthenticatedUser;
import org.example.policyservice.underwriting.dto.response.FarmRiskProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client
        .WebClientRequestException;

import reactor.core.publisher.Mono;

@Component
public class FarmServiceClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FarmServiceClient.class);

    private final WebClient webClient;

    public FarmServiceClient(
            WebClient.Builder loadBalancedWebClientBuilder) {

        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://FARM-SERVICE")
                .build();
    }

    public Mono<FarmApplicationValidationResponse>
    validateFarmAndCrop(
            AuthenticatedUser user,
            Long farmId,
            Long cropId) {

        return webClient
                .get()
                .uri(
                        "/api/farms/{farmId}/crops/{cropId}/application-validation",
                        farmId,
                        cropId
                )
                .header(
                        "X-User-Id",
                        user.userId().toString()
                )
                .header(
                        "X-User-Email",
                        user.email()
                )
                .header(
                        "X-User-Roles",
                        "ROLE_FARMER"
                )
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response -> Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "FARM_OR_CROP_NOT_FOUND",
                                        "Selected farm or crop was not found"
                                )
                        )
                )
                .onStatus(
                        status -> status.value() == 409,
                        response -> Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "FARM_PROFILE_INCOMPLETE",
                                        "Complete farm, crop, and irrigation details before applying"
                                )
                        )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(
                                new ApiException(
                                        HttpStatus.SERVICE_UNAVAILABLE,
                                        "FARM_SERVICE_UNAVAILABLE",
                                        "Farm verification is temporarily unavailable"
                                )
                        )
                )
                .bodyToMono(
                        FarmApplicationValidationResponse.class
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        exception -> new ApiException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "FARM_SERVICE_UNAVAILABLE",
                                "Farm verification is temporarily unavailable"
                        )
                );
    }

    public Mono<FarmRiskProfileResponse> getFarmRiskProfile(
            AuthenticatedUser underwriter,
            Long farmId) {

        return webClient
                .get()
                .uri(
                        "/api/farms/{farmId}/risk-profile",
                        farmId
                )
                .header(
                        "X-User-Id",
                        underwriter.userId().toString()
                )
                .header(
                        "X-User-Email",
                        underwriter.email()
                )
                .header(
                        "X-User-Roles",
                        "ROLE_UNDERWRITER"
                )
                .retrieve()

                .onStatus(
                        status -> status.value() == 404,
                        response -> Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "FARM_RISK_PROFILE_NOT_FOUND",
                                        "Farm risk profile was not found"
                                )
                        )
                )

                .onStatus(
                        status -> status.value() == 409,
                        response -> Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "FARM_RISK_PROFILE_INCOMPLETE",
                                        "Farm risk information is incomplete"
                                )
                        )
                )

                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(
                                new ApiException(
                                        HttpStatus.SERVICE_UNAVAILABLE,
                                        "FARM_SERVICE_UNAVAILABLE",
                                        "Farm risk information is temporarily unavailable"
                                )
                        )
                )

                .bodyToMono(FarmRiskProfileResponse.class)

                .onErrorMap(
                        WebClientRequestException.class,
                        exception -> new ApiException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "FARM_SERVICE_UNAVAILABLE",
                                "Farm risk information is temporarily unavailable"
                        )
                );
    }
}