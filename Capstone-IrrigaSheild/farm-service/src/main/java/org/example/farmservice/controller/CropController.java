package org.example.farmservice.controller;

import org.example.farmservice.dto.request.CropRequest;
import org.example.farmservice.dto.response.CropResponse;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.service.CropService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/farms/{farmId}/crops")
@PreAuthorize("hasRole('FARMER')")
public class CropController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CropController.class);

    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PostMapping
    public Mono<ResponseEntity<CropResponse>> createCrop(
            Authentication authentication,
            @PathVariable Long farmId,
            @Valid @RequestBody CropRequest request) {

        return cropService
                .createCrop(
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
    public Flux<CropResponse> getCrops(
            Authentication authentication,
            @PathVariable Long farmId) {

        return cropService.getCrops(
                getUser(authentication),
                farmId
        );
    }

    @GetMapping("/{cropId}")
    public Mono<ResponseEntity<CropResponse>> getCrop(
            Authentication authentication,
            @PathVariable Long farmId,
            @PathVariable Long cropId) {

        return cropService
                .getCrop(
                        getUser(authentication),
                        farmId,
                        cropId
                )
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{cropId}")
    public Mono<ResponseEntity<CropResponse>> updateCrop(
            Authentication authentication,
            @PathVariable Long farmId,
            @PathVariable Long cropId,
            @Valid @RequestBody CropRequest request) {

        return cropService
                .updateCrop(
                        getUser(authentication),
                        farmId,
                        cropId,
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