package org.example.farmservice.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.example.farmservice.dto.request
        .IrrigationProfileRequest;
import org.example.farmservice.dto.response
        .IrrigationProfileResponse;
import org.example.farmservice.entity.Farm;
import org.example.farmservice.entity.IrrigationProfile;
import org.example.farmservice.exception.ApiException;
import org.example.farmservice.repository.FarmRepository;
import org.example.farmservice.repository
        .FarmerProfileRepository;
import org.example.farmservice.repository
        .IrrigationProfileRepository;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.util
        .IrrigationProfileMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class IrrigationProfileServiceImpl
        implements IrrigationProfileService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IrrigationProfileServiceImpl.class);

    private final IrrigationProfileRepository irrigationRepository;
    private final FarmRepository farmRepository;
    private final FarmerProfileRepository farmerProfileRepository;

    public IrrigationProfileServiceImpl(
            IrrigationProfileRepository irrigationRepository,
            FarmRepository farmRepository,
            FarmerProfileRepository farmerProfileRepository) {

        this.irrigationRepository = irrigationRepository;
        this.farmRepository = farmRepository;
        this.farmerProfileRepository = farmerProfileRepository;
    }

    @Override
    public Mono<IrrigationProfileResponse> createProfile(
            AuthenticatedUser user,
            Long farmId,
            IrrigationProfileRequest request) {

        return findOwnedFarm(user.userId(), farmId)
                .flatMap(farm ->
                        irrigationRepository
                                .existsByFarmId(farm.getFarmId())
                                .flatMap(exists -> {

                                    if (exists) {
                                        return Mono.error(
                                                new ApiException(
                                                        HttpStatus.CONFLICT,
                                                        "IRRIGATION_PROFILE_EXISTS",
                                                        "Irrigation profile already exists"
                                                )
                                        );
                                    }

                                    IrrigationProfile profile =
                                            createEntity(
                                                    farm.getFarmId(),
                                                    request
                                            );

                                    return irrigationRepository
                                            .save(profile);
                                })
                )
                .map(IrrigationProfileMapper::toResponse);
    }

    @Override
    public Mono<IrrigationProfileResponse> getProfile(
            AuthenticatedUser user,
            Long farmId) {

        return findOwnedFarm(user.userId(), farmId)
                .flatMap(farm ->
                        findIrrigationProfile(
                                farm.getFarmId()
                        )
                )
                .map(IrrigationProfileMapper::toResponse);
    }

    @Override
    public Mono<IrrigationProfileResponse> updateProfile(
            AuthenticatedUser user,
            Long farmId,
            IrrigationProfileRequest request) {

        return findOwnedFarm(user.userId(), farmId)
                .flatMap(farm ->
                        findIrrigationProfile(
                                farm.getFarmId()
                        )
                )
                .flatMap(profile -> {

                    updateEntity(profile, request);

                    return irrigationRepository.save(profile);
                })
                .map(IrrigationProfileMapper::toResponse);
    }

    private Mono<Farm> findOwnedFarm(
            Long authUserId,
            Long farmId) {

        return farmerProfileRepository
                .findByAuthUserId(authUserId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "FARMER_PROFILE_NOT_FOUND",
                                        "Farmer profile was not found"
                                )
                        )
                )
                .flatMap(profile ->
                        farmRepository.findByFarmIdAndFarmerId(
                                farmId,
                                profile.getFarmerId()
                        )
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

    private Mono<IrrigationProfile> findIrrigationProfile(
            Long farmId) {

        return irrigationRepository
                .findByFarmId(farmId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "IRRIGATION_PROFILE_NOT_FOUND",
                                        "Irrigation profile was not found"
                                )
                        )
                );
    }

    private IrrigationProfile createEntity(
            Long farmId,
            IrrigationProfileRequest request) {

        LocalDateTime now = LocalDateTime.now();

        return IrrigationProfile.builder()
                .farmId(farmId)
                .waterSource(normalize(request.waterSource()))
                .irrigationMethod(
                        normalize(request.irrigationMethod())
                )
                .equipmentCondition(
                        normalize(request.equipmentCondition())
                )
                .backupWaterSource(
                        normalizeOptional(
                                request.backupWaterSource()
                        )
                )
                .historicalFailureCount(
                        request.historicalFailureCount()
                )
                .averageWaterAvailabilityPercentage(
                        request.averageWaterAvailabilityPercentage()
                )
                .seasonalRisk(
                        normalize(request.seasonalRisk())
                )
                .maintenanceFrequency(
                        normalize(request.maintenanceFrequency())
                )
                .lastMaintenanceDate(
                        request.lastMaintenanceDate()
                )
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private void updateEntity(
            IrrigationProfile profile,
            IrrigationProfileRequest request) {

        profile.setWaterSource(
                normalize(request.waterSource())
        );

        profile.setIrrigationMethod(
                normalize(request.irrigationMethod())
        );

        profile.setEquipmentCondition(
                normalize(request.equipmentCondition())
        );

        profile.setBackupWaterSource(
                normalizeOptional(
                        request.backupWaterSource()
                )
        );

        profile.setHistoricalFailureCount(
                request.historicalFailureCount()
        );

        profile.setAverageWaterAvailabilityPercentage(
                request.averageWaterAvailabilityPercentage()
        );

        profile.setSeasonalRisk(
                normalize(request.seasonalRisk())
        );

        profile.setMaintenanceFrequency(
                normalize(request.maintenanceFrequency())
        );

        profile.setLastMaintenanceDate(
                request.lastMaintenanceDate()
        );

        profile.setUpdatedAt(LocalDateTime.now());
    }

    private String normalize(String value) {

        return value.trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return normalize(value);
    }
}