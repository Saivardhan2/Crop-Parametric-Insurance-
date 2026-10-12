package org.example.farmservice.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.example.farmservice.dto.request.FarmRequest;
import org.example.farmservice.dto.response.FarmResponse;
import org.example.farmservice.entity.Farm;
import org.example.farmservice.exception.ApiException;
import org.example.farmservice.repository.FarmRepository;
import org.example.farmservice.repository.FarmerProfileRepository;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.util.FarmMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FarmServiceImpl implements FarmService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FarmServiceImpl.class);

    private final FarmRepository farmRepository;
    private final FarmerProfileRepository farmerProfileRepository;

    public FarmServiceImpl(
            FarmRepository farmRepository,
            FarmerProfileRepository farmerProfileRepository) {

        this.farmRepository = farmRepository;
        this.farmerProfileRepository =
                farmerProfileRepository;
    }

    @Override
    public Mono<FarmResponse> createFarm(
            AuthenticatedUser authenticatedUser,
            FarmRequest request) {

        return findFarmerId(authenticatedUser.userId())
                .flatMap(farmerId ->
                        farmRepository
                                .existsByFarmerIdAndFarmNameIgnoreCase(
                                        farmerId,
                                        request.farmName().trim()
                                )
                                .flatMap(exists -> {

                                    if (exists) {
                                        return Mono.error(
                                                new ApiException(
                                                        HttpStatus.CONFLICT,
                                                        "FARM_ALREADY_EXISTS",
                                                        "A farm with this name already exists"
                                                )
                                        );
                                    }

                                    Farm farm = createFarmEntity(
                                            farmerId,
                                            request
                                    );

                                    return farmRepository.save(farm);
                                })
                )
                .map(FarmMapper::toResponse);
    }

    @Override
    public Flux<FarmResponse> getMyFarms(
            AuthenticatedUser authenticatedUser) {

        return findFarmerId(authenticatedUser.userId())
                .flatMapMany(farmerId ->
                        farmRepository
                                .findAllByFarmerIdOrderByCreatedAtDesc(
                                        farmerId
                                )
                )
                .map(FarmMapper::toResponse);
    }

    @Override
    public Mono<FarmResponse> getMyFarmById(
            AuthenticatedUser authenticatedUser,
            Long farmId) {

        return findFarmerId(authenticatedUser.userId())
                .flatMap(farmerId ->
                        findOwnedFarm(farmId, farmerId)
                )
                .map(FarmMapper::toResponse);
    }

    @Override
    public Mono<FarmResponse> updateMyFarm(
            AuthenticatedUser authenticatedUser,
            Long farmId,
            FarmRequest request) {

        return findFarmerId(authenticatedUser.userId())
                .flatMap(farmerId ->
                        findOwnedFarm(farmId, farmerId)
                )
                .flatMap(farm -> {

                    updateFarmEntity(farm, request);

                    return farmRepository.save(farm);
                })
                .map(FarmMapper::toResponse);
    }

    private Mono<Long> findFarmerId(Long authUserId) {

        return farmerProfileRepository
                .findByAuthUserId(authUserId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "FARMER_PROFILE_NOT_FOUND",
                                        "Create a farmer profile before registering a farm"
                                )
                        )
                )
                .map(profile -> profile.getFarmerId());
    }

    private Mono<Farm> findOwnedFarm(
            Long farmId,
            Long farmerId) {

        return farmRepository
                .findByFarmIdAndFarmerId(
                        farmId,
                        farmerId
                )
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "FARM_NOT_FOUND",
                                        "Farm was not found"
                                )
                        )
                );
    }

    private Farm createFarmEntity(
            Long farmerId,
            FarmRequest request) {

        LocalDateTime now = LocalDateTime.now();

        return Farm.builder()
                .farmerId(farmerId)
                .farmName(request.farmName().trim())
                .surveyNumber(
                        normalizeOptional(
                                request.surveyNumber()
                        )
                )
                .addressLine(request.addressLine().trim())
                .village(request.village().trim())
                .district(request.district().trim())
                .state(request.state().trim())
                .postalCode(request.postalCode().trim())
                .areaInAcres(request.areaInAcres())
                .soilType(
                        request.soilType()
                                .trim()
                                .toUpperCase(Locale.ROOT)
                )
                .ownershipType(
                        request.ownershipType()
                                .trim()
                                .toUpperCase(Locale.ROOT)
                )
                .latitude(request.latitude())
                .longitude(request.longitude())
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private void updateFarmEntity(
            Farm farm,
            FarmRequest request) {

        farm.setFarmName(
                request.farmName().trim()
        );

        farm.setSurveyNumber(
                normalizeOptional(
                        request.surveyNumber()
                )
        );

        farm.setAddressLine(
                request.addressLine().trim()
        );

        farm.setVillage(
                request.village().trim()
        );

        farm.setDistrict(
                request.district().trim()
        );

        farm.setState(
                request.state().trim()
        );

        farm.setPostalCode(
                request.postalCode().trim()
        );

        farm.setAreaInAcres(
                request.areaInAcres()
        );

        farm.setSoilType(
                request.soilType()
                        .trim()
                        .toUpperCase(Locale.ROOT)
        );

        farm.setOwnershipType(
                request.ownershipType()
                        .trim()
                        .toUpperCase(Locale.ROOT)
        );

        farm.setLatitude(request.latitude());
        farm.setLongitude(request.longitude());
        farm.setUpdatedAt(LocalDateTime.now());
    }

    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
