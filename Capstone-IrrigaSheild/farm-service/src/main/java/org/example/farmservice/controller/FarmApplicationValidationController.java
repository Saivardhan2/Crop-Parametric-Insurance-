package org.example.farmservice.controller;

import org.example.farmservice.dto.response
        .FarmApplicationValidationResponse;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.service
        .FarmApplicationValidationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/farms")
public class FarmApplicationValidationController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FarmApplicationValidationController.class);

    private final FarmApplicationValidationService
            validationService;

    public FarmApplicationValidationController(
            FarmApplicationValidationService validationService) {

        this.validationService = validationService;
    }

    @GetMapping(
            "/{farmId}/crops/{cropId}/application-validation"
    )
    @PreAuthorize("hasRole('FARMER')")
    public Mono<ResponseEntity<
            FarmApplicationValidationResponse>>
    validateForApplication(
            Authentication authentication,
            @PathVariable Long farmId,
            @PathVariable Long cropId) {

        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        return validationService
                .validateForApplication(
                        user,
                        farmId,
                        cropId
                )
                .map(ResponseEntity::ok);
    }
}