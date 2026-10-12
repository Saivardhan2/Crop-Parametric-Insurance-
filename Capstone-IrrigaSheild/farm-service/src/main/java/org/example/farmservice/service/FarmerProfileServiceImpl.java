package org.example.farmservice.service;

import java.time.LocalDateTime;

import org.example.farmservice.dto.request
        .FarmerProfileRequest;
import org.example.farmservice.dto.response
        .FarmerProfileResponse;
import org.example.farmservice.entity.FarmerProfile;
import org.example.farmservice.exception.ApiException;
import org.example.farmservice.repository
        .FarmerProfileRepository;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.util.FarmerProfileMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class FarmerProfileServiceImpl
        implements FarmerProfileService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FarmerProfileServiceImpl.class);

    private final FarmerProfileRepository repository;

    public FarmerProfileServiceImpl(
            FarmerProfileRepository repository) {

        this.repository = repository;
    }

    @Override
    public Mono<FarmerProfileResponse> createProfile(
            AuthenticatedUser authenticatedUser,
            FarmerProfileRequest request) {

        return repository
                .existsByAuthUserId(
                        authenticatedUser.userId()
                )
                .flatMap(profileExists -> {

                    if (profileExists) {
                        return Mono.error(
                                new ApiException(
                                        HttpStatus.CONFLICT,
                                        "PROFILE_ALREADY_EXISTS",
                                        "Farmer profile already exists"
                                )
                        );
                    }

                    LocalDateTime now =
                            LocalDateTime.now();

                    FarmerProfile profile =
                            FarmerProfile.builder()
                                    .authUserId(
                                            authenticatedUser
                                                    .userId()
                                    )
                                    .email(
                                            authenticatedUser
                                                    .email()
                                    )
                                    .fullName(
                                            request.fullName()
                                                    .trim()
                                    )
                                    .phoneNumber(
                                            request.phoneNumber()
                                                    .trim()
                                    )
                                    .addressLine(
                                            request.addressLine()
                                                    .trim()
                                    )
                                    .village(
                                            request.village()
                                                    .trim()
                                    )
                                    .district(
                                            request.district()
                                                    .trim()
                                    )
                                    .state(
                                            request.state()
                                                    .trim()
                                    )
                                    .postalCode(
                                            request.postalCode()
                                                    .trim()
                                    )
                                    .bankAccountLastFour(
                                            normalizeOptional(
                                                    request
                                                            .bankAccountLastFour()
                                            )
                                    )
                                    .verificationStatus(
                                            "PENDING"
                                    )
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build();

                    return repository
                            .save(profile)
                            .map(
                                    FarmerProfileMapper
                                            ::toResponse
                            );
                });
    }

    @Override
    public Mono<FarmerProfileResponse> getMyProfile(
            AuthenticatedUser authenticatedUser) {

        return repository
                .findByAuthUserId(
                        authenticatedUser.userId()
                )
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "PROFILE_NOT_FOUND",
                                        "Farmer profile was not found"
                                )
                        )
                )
                .map(FarmerProfileMapper::toResponse);
    }

    @Override
    public Mono<FarmerProfileResponse> updateMyProfile(
            AuthenticatedUser authenticatedUser,
            FarmerProfileRequest request) {

        return repository
                .findByAuthUserId(
                        authenticatedUser.userId()
                )
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "PROFILE_NOT_FOUND",
                                        "Farmer profile was not found"
                                )
                        )
                )
                .flatMap(profile -> {

                    profile.setFullName(
                            request.fullName().trim()
                    );

                    profile.setPhoneNumber(
                            request.phoneNumber().trim()
                    );

                    profile.setAddressLine(
                            request.addressLine().trim()
                    );

                    profile.setVillage(
                            request.village().trim()
                    );

                    profile.setDistrict(
                            request.district().trim()
                    );

                    profile.setState(
                            request.state().trim()
                    );

                    profile.setPostalCode(
                            request.postalCode().trim()
                    );

                    profile.setBankAccountLastFour(
                            normalizeOptional(
                                    request.bankAccountLastFour()
                            )
                    );

                    profile.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return repository.save(profile);
                })
                .map(FarmerProfileMapper::toResponse);
    }

    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}