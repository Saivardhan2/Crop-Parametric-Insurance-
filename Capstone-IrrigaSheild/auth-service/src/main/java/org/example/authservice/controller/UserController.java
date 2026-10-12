package org.example.authservice.controller;

import java.security.Principal;

import org.example.authservice.dto.response.UserResponse;
import org.example.authservice.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserController.class);

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserResponse>> getCurrentUser(
            Mono<Principal> principal) {

        return principal
                .flatMap(authenticatedUser ->
                        authService.getCurrentUser(
                                authenticatedUser.getName()
                        )
                )
                .map(ResponseEntity::ok);
    }
}
