package org.example.farmservice.controller;

import org.example.farmservice.dto.request
        .FarmerProfileRequest;
import org.example.farmservice.dto.response
        .FarmerProfileResponse;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.service
        .FarmerProfileService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/farmers/profile")
public class FarmerProfileController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FarmerProfileController.class);

    private final FarmerProfileService profileService;

    public FarmerProfileController(
            FarmerProfileService profileService) {

        this.profileService = profileService;
    }

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public Mono<ResponseEntity<FarmerProfileResponse>>
    createProfile(
            Authentication authentication,

            @Valid
            @RequestBody
            FarmerProfileRequest request) {

        AuthenticatedUser authenticatedUser =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        return profileService
                .createProfile(
                        authenticatedUser,
                        request
                )
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('FARMER')")
    public Mono<ResponseEntity<FarmerProfileResponse>>
    getMyProfile(
            Authentication authentication) {

        AuthenticatedUser authenticatedUser =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        return profileService
                .getMyProfile(authenticatedUser)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('FARMER')")
    public Mono<ResponseEntity<FarmerProfileResponse>>
    updateMyProfile(
            Authentication authentication,

            @Valid
            @RequestBody
            FarmerProfileRequest request) {

        AuthenticatedUser authenticatedUser =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        return profileService
                .updateMyProfile(
                        authenticatedUser,
                        request
                )
                .map(ResponseEntity::ok);
    }
}