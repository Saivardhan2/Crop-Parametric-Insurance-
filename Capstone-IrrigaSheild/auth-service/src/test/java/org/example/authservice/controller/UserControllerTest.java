package org.example.authservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.example.authservice.dto.response.UserResponse;
import org.example.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class UserControllerTest {

    private AuthService authService;
    private UserController controller;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        controller = new UserController(authService);
    }

    @Test
    void shouldGetCurrentUser() {
        UserResponse response = new UserResponse(5L, "Farmer Alice", "alice@example.com", "FARMER", "ACTIVE");
        when(authService.getCurrentUser("alice@example.com")).thenReturn(Mono.just(response));

        Principal principal = () -> "alice@example.com";

        StepVerifier.create(controller.getCurrentUser(Mono.just(principal)))
                .assertNext(res -> {
                    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(res.getBody()).isNotNull();
                    assertThat(res.getBody().userId()).isEqualTo(5L);
                    assertThat(res.getBody().email()).isEqualTo("alice@example.com");
                })
                .verifyComplete();
    }
}
