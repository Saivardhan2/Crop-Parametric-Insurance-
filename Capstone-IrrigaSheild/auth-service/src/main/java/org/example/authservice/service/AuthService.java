package org.example.authservice.service;

import org.example.authservice.dto.request.CreateUserRequest;
import org.example.authservice.dto.request.LoginRequest;
import org.example.authservice.dto.request.RegisterRequest;
import org.example.authservice.dto.response.LoginResult;
import org.example.authservice.dto.response.UserResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<UserResponse> register(RegisterRequest request);

    Mono<LoginResult> login(LoginRequest request);

    Mono<UserResponse> getCurrentUser(String email);

    Mono<UserResponse> createInternalUser(
            CreateUserRequest request
    );

    Flux<UserResponse> getAllUsers();

    Mono<UserResponse> updateUserStatus(Long userId, String status);
}