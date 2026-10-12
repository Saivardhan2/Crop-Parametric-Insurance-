package org.example.authservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.example.authservice.dto.request.LoginRequest;
import org.example.authservice.dto.response.LoginResult;
import org.example.authservice.exception.ApiException;
import org.example.authservice.exception.GlobalExceptionHandler;
import org.example.authservice.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

import java.time.Duration;

class AuthControllerIntegrationTest {

    private static final String TOKEN_COOKIE =
            "IRRIGASHIELD_TOKEN";

    private AuthService authService;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {

        authService = mock(AuthService.class);

        AuthController authController =
                new AuthController(
                        authService,
                        false
                );

        webTestClient = WebTestClient
                .bindToController(authController)
                .controllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }

    @Test
    void validLoginShouldReturnCookie() {

        LoginResult loginResult =
                new LoginResult(
                        "test-jwt-token",
                        3600L,
                        null
                );

        when(authService.login(
                any(LoginRequest.class)
        )).thenReturn(
                Mono.just(loginResult)
        );

        webTestClient
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validLoginRequest())
                .exchange()
                .expectStatus()
                .isOk()
                .expectCookie()
                .valueEquals(
                        TOKEN_COOKIE,
                        "test-jwt-token"
                )
                .expectCookie()
                .httpOnly(TOKEN_COOKIE, true)
                .expectCookie()
                .path(TOKEN_COOKIE, "/")
                .expectBody()
                .jsonPath("$.expiresIn")
                .isEqualTo(3600);
    }

    @Test
    void invalidLoginShouldReturnUnauthorized() {

        when(authService.login(
                any(LoginRequest.class)
        )).thenReturn(
                Mono.error(
                        new ApiException(
                                HttpStatus.UNAUTHORIZED,
                                "INVALID_CREDENTIALS",
                                "Invalid email or password"
                        )
                )
        );

        webTestClient
                .post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validLoginRequest())
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .jsonPath("$.errorCode")
                .isEqualTo("INVALID_CREDENTIALS")
                .jsonPath("$.message")
                .isEqualTo(
                        "Invalid email or password"
                );
    }

    @Test
    void logoutShouldDeleteAuthenticationCookie() {

        webTestClient
                .post()
                .uri("/api/auth/logout")
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectCookie()
                .exists(TOKEN_COOKIE)
                .expectCookie()
                .maxAge(TOKEN_COOKIE, Duration.ofDays(0));
    }

    private String validLoginRequest() {

        return """
                {
                  "email": "farmer@example.com",
                  "password": "Password123"
                }
                """;
    }
}