package org.example.authservice.controller;

import java.time.Duration;
import org.example.authservice.dto.request.LoginRequest;
import org.example.authservice.dto.request.RegisterRequest;
import org.example.authservice.dto.response.AuthResponse;
import org.example.authservice.dto.response.UserResponse;
import org.example.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);
    private static final String TOKEN_COOKIE = "IRRIGASHIELD_TOKEN";

    private final AuthService authService;
    private final boolean secureCookie;

    public AuthController(AuthService authService, @Value("${security.cookie.secure:false}") boolean secureCookie) {
        this.authService = authService;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request)
                .map(response -> ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request, ServerWebExchange exchange) {
        return authService.login(request)
                .map(loginResult -> {
                    ResponseCookie tokenCookie = ResponseCookie
                                    .from(TOKEN_COOKIE, loginResult.accessToken())
                                    .httpOnly(true)
                                    .secure(secureCookie)
                                    .sameSite("Lax")
                                    .path("/")
                                    .maxAge(Duration.ofSeconds(loginResult.expiresIn()))
                                    .build();
                    exchange.getResponse().addCookie(tokenCookie);

                    AuthResponse response = new AuthResponse("Bearer", loginResult.accessToken(), loginResult.expiresIn(), loginResult.user());

                    return ResponseEntity.ok(response);
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        ResponseCookie deletedCookie = ResponseCookie
                        .from(TOKEN_COOKIE, "")
                        .httpOnly(true)
                        .secure(secureCookie)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(Duration.ZERO)
                        .build();

        exchange.getResponse().addCookie(deletedCookie);

        return Mono.just(ResponseEntity.noContent().build());
    }
}