package org.example.farmservice.controller;

import org.example.farmservice.dto.request
        .IrrigationProfileRequest;
import org.example.farmservice.dto.response
        .IrrigationProfileResponse;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.service
        .IrrigationProfileService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/farms/{farmId}/irrigation")
@PreAuthorize("hasRole('FARMER')")
public class IrrigationProfileController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IrrigationProfileController.class);

    private final IrrigationProfileService irrigationService;

    public IrrigationProfileController(
            IrrigationProfileService irrigationService) {

        this.irrigationService = irrigationService;
    }

    @PostMapping
    public Mono<ResponseEntity<IrrigationProfileResponse>>
    createProfile(
            Authentication authentication,
            @PathVariable Long farmId,
            @Valid @RequestBody
            IrrigationProfileRequest request) {

        return irrigationService
                .createProfile(
                        getUser(authentication),
                        farmId,
                        request
                )
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @GetMapping
    public Mono<ResponseEntity<IrrigationProfileResponse>>
    getProfile(
            Authentication authentication,
            @PathVariable Long farmId) {

        return irrigationService
                .getProfile(
                        getUser(authentication),
                        farmId
                )
                .map(ResponseEntity::ok);
    }

    @PutMapping
    public Mono<ResponseEntity<IrrigationProfileResponse>>
    updateProfile(
            Authentication authentication,
            @PathVariable Long farmId,
            @Valid @RequestBody
            IrrigationProfileRequest request) {

        return irrigationService
                .updateProfile(
                        getUser(authentication),
                        farmId,
                        request
                )
                .map(ResponseEntity::ok);
    }

    private AuthenticatedUser getUser(
            Authentication authentication) {

        return (AuthenticatedUser)
                authentication.getPrincipal();
    }
}