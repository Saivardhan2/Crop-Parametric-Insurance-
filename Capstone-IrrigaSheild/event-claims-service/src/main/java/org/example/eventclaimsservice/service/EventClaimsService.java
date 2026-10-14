package org.example.eventclaimsservice.service;

import org.example.eventclaimsservice.dto.ClaimDecisionRequest;
import org.example.eventclaimsservice.dto.ClaimFeedbackRequest;
import org.example.eventclaimsservice.dto.ClaimResponse;
import org.example.eventclaimsservice.dto.IrrigationReadingRequest;
import org.example.eventclaimsservice.security.AuthenticatedUser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventClaimsService {

     Mono<ClaimResponse> processReading(
             AuthenticatedUser dataProvider,
             IrrigationReadingRequest request
     );

     Flux<ClaimResponse> getMyClaims(
             AuthenticatedUser farmer
     );

     Flux<ClaimResponse> getAllClaims(
             AuthenticatedUser user
     );

     Mono<ClaimResponse> approveClaim(
             AuthenticatedUser officer,
             Long claimId,
             ClaimDecisionRequest request
     );

     Mono<ClaimResponse> rejectClaim(
             AuthenticatedUser officer,
             Long claimId,
             ClaimDecisionRequest request
     );

     Mono<ClaimResponse> submitFeedback(
             AuthenticatedUser farmer,
             Long claimId,
             ClaimFeedbackRequest request
     );
}