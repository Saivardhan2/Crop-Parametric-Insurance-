package org.example.authservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.example.authservice.dto.request.CreateUserRequest;
import org.example.authservice.dto.response.UserResponse;
import org.example.authservice.exception.GlobalExceptionHandler;
import org.example.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AdminUserControllerTest {

    private AuthService authService;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        AdminUserController controller = new AdminUserController(authService);
        webTestClient = WebTestClient
                .bindToController(controller)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateInternalUser() {
        UserResponse response = new UserResponse(10L, "New UW", "uw@irrigashield.com", "UNDERWRITER", "ACTIVE");
        when(authService.createInternalUser(any(CreateUserRequest.class))).thenReturn(Mono.just(response));

        String requestJson = "{\"name\":\"New UW\",\"email\":\"uw@irrigashield.com\",\"password\":\"Password123\",\"role\":\"UNDERWRITER\"}";

        webTestClient.post()
                .uri("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestJson)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(10)
                .jsonPath("$.email").isEqualTo("uw@irrigashield.com");
    }

    @Test
    void shouldGetAllUsers() {
        UserResponse user = new UserResponse(1L, "Admin", "admin@irrigashield.com", "ADMIN", "ACTIVE");
        when(authService.getAllUsers()).thenReturn(Flux.just(user));

        webTestClient.get()
                .uri("/api/admin/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].email").isEqualTo("admin@irrigashield.com");
    }

    @Test
    void shouldUpdateUserStatus() {
        UserResponse updated = new UserResponse(2L, "Farmer Bob", "bob@example.com", "FARMER", "INACTIVE");
        when(authService.updateUserStatus(eq(2L), eq("INACTIVE"))).thenReturn(Mono.just(updated));

        webTestClient.put()
                .uri("/api/admin/users/2/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "INACTIVE"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("INACTIVE");
    }

    @Test
    void shouldReturnBadRequestWhenStatusMissing() {
        webTestClient.put()
                .uri("/api/admin/users/2/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("INVALID_STATUS");
    }
}
