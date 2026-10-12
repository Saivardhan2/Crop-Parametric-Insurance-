package org.example.authservice.controller;

import org.example.authservice.dto.request.CreateUserRequest;
import org.example.authservice.dto.response.UserResponse;
import org.example.authservice.exception.ApiException;
import org.example.authservice.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminUserController.class);

    private final AuthService authService;

    public AdminUserController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public Mono<ResponseEntity<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return authService
                .createInternalUser(request)
                .map(response ->
                        ResponseEntity
                               .status(HttpStatus.CREATED)
                               .body(response)
                );
    }

    @GetMapping
    public Flux<UserResponse> getAllUsers() {

        return authService.getAllUsers();
    }

    @PutMapping("/{userId}/status")
    public Mono<ResponseEntity<UserResponse>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        if (status == null || status.isBlank()) {
            return Mono.error(
                    new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "INVALID_STATUS",
                            "Status is required"
                    )
            );
        }

        return authService
                .updateUserStatus(userId, status)
                .map(ResponseEntity::ok);
    }
}
