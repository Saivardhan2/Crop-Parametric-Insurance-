package org.example.eventclaimsservice.controller;

import jakarta.validation.Valid;

import org.example.eventclaimsservice.dto.ClaimResponse;
import org.example.eventclaimsservice.dto.IrrigationReadingRequest;
import org.example.eventclaimsservice.security.AuthenticatedUser;
import org.example.eventclaimsservice.service.EventClaimsService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class EventClaimsController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventClaimsController.class);

    private final EventClaimsService eventClaimsService;

    public EventClaimsController(EventClaimsService eventClaimsService) {
        this.eventClaimsService = eventClaimsService;
    }

    @PostMapping("/readings")
    @PreAuthorize("hasRole('DATA_PROVIDER')")
    public Mono<ResponseEntity<ClaimResponse>> submitReading(
            Authentication authentication,
            @Valid @RequestBody IrrigationReadingRequest request) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        return eventClaimsService
                .processReading(user, request)
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @GetMapping("/claims/my-claims")
    @PreAuthorize("hasRole('FARMER')")
    public Flux<ClaimResponse> getMyClaims(
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        return eventClaimsService.getMyClaims(user);
    }
}