package org.example.farmservice.controller;

import org.example.farmservice.dto.request.FarmRequest;
import org.example.farmservice.dto.response.FarmResponse;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.service.FarmService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/farms")
@PreAuthorize("hasRole('FARMER')")
public class FarmController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FarmController.class);

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    @PostMapping
    public Mono<ResponseEntity<FarmResponse>> createFarm(
            Authentication authentication,
            @Valid @RequestBody FarmRequest request) {

        AuthenticatedUser authenticatedUser =
                getAuthenticatedUser(authentication);

        return farmService
                .createFarm(authenticatedUser, request)
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @GetMapping("/my-farms")
    public Flux<FarmResponse> getMyFarms(
            Authentication authentication) {

        return farmService.getMyFarms(
                getAuthenticatedUser(authentication)
        );
    }

    @GetMapping("/{farmId}")
    public Mono<ResponseEntity<FarmResponse>> getFarmById(
            Authentication authentication,
            @PathVariable Long farmId) {

        return farmService
                .getMyFarmById(
                        getAuthenticatedUser(authentication),
                        farmId
                )
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{farmId}")
    public Mono<ResponseEntity<FarmResponse>> updateFarm(
            Authentication authentication,
            @PathVariable Long farmId,
            @Valid @RequestBody FarmRequest request) {

        return farmService
                .updateMyFarm(
                        getAuthenticatedUser(authentication),
                        farmId,
                        request
                )
                .map(ResponseEntity::ok);
    }

    private AuthenticatedUser getAuthenticatedUser(
            Authentication authentication) {

        return (AuthenticatedUser)
                authentication.getPrincipal();
    }
}