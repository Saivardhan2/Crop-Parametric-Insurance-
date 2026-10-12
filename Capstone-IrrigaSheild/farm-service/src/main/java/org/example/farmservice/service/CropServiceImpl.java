package org.example.farmservice.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.example.farmservice.dto.request.CropRequest;
import org.example.farmservice.dto.response.CropResponse;
import org.example.farmservice.entity.Crop;
import org.example.farmservice.entity.Farm;
import org.example.farmservice.exception.ApiException;
import org.example.farmservice.repository.CropRepository;
import org.example.farmservice.repository.FarmRepository;
import org.example.farmservice.repository.FarmerProfileRepository;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.util.CropMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CropServiceImpl implements CropService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CropServiceImpl.class);

    private final CropRepository cropRepository;
    private final FarmRepository farmRepository;
    private final FarmerProfileRepository profileRepository;

    public CropServiceImpl(
            CropRepository cropRepository,
            FarmRepository farmRepository,
            FarmerProfileRepository profileRepository) {

        this.cropRepository = cropRepository;
        this.farmRepository = farmRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public Mono<CropResponse> createCrop(
            AuthenticatedUser user,
            Long farmId,
            CropRequest request) {

        return findOwnedFarm(user.userId(), farmId)
                .flatMap(farm -> {

                    validateDates(request);

                    LocalDateTime now = LocalDateTime.now();

                    Crop crop = Crop.builder()
                            .farmId(farm.getFarmId())
                            .cropType(normalize(request.cropType()))
                            .variety(
                                    normalizeOptional(request.variety())
                            )
                            .sowingDate(request.sowingDate())
                            .expectedHarvestDate(
                                    request.expectedHarvestDate()
                            )
                            .growthStage(
                                    normalize(request.growthStage())
                            )
                            .irrigationRequirement(
                                    normalize(
                                            request.irrigationRequirement()
                                    )
                            )
                            .status("ACTIVE")
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    return cropRepository.save(crop);
                })
                .map(CropMapper::toResponse);
    }

    @Override
    public Flux<CropResponse> getCrops(
            AuthenticatedUser user,
            Long farmId) {

        return findOwnedFarm(user.userId(), farmId)
                .flatMapMany(farm ->
                        cropRepository
                                .findAllByFarmIdOrderByCreatedAtDesc(
                                        farm.getFarmId()
                                )
                )
                .map(CropMapper::toResponse);
    }

    @Override
    public Mono<CropResponse> getCrop(
            AuthenticatedUser user,
            Long farmId,
            Long cropId) {

        return findOwnedFarm(user.userId(), farmId)
                .flatMap(farm ->
                        findCrop(cropId, farm.getFarmId())
                )
                .map(CropMapper::toResponse);
    }

    @Override
    public Mono<CropResponse> updateCrop(
            AuthenticatedUser user,
            Long farmId,
            Long cropId,
            CropRequest request) {

        return findOwnedFarm(user.userId(), farmId)
                .flatMap(farm ->
                        findCrop(cropId, farm.getFarmId())
                )
                .flatMap(crop -> {

                    validateDates(request);

                    crop.setCropType(
                            normalize(request.cropType())
                    );

                    crop.setVariety(
                            normalizeOptional(request.variety())
                    );

                    crop.setSowingDate(
                            request.sowingDate()
                    );

                    crop.setExpectedHarvestDate(
                            request.expectedHarvestDate()
                    );

                    crop.setGrowthStage(
                            normalize(request.growthStage())
                    );

                    crop.setIrrigationRequirement(
                            normalize(
                                    request.irrigationRequirement()
                            )
                    );

                    crop.setUpdatedAt(LocalDateTime.now());

                    return cropRepository.save(crop);
                })
                .map(CropMapper::toResponse);
    }

    private Mono<Farm> findOwnedFarm(
            Long authUserId,
            Long farmId) {

        return profileRepository
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

    private Mono<Crop> findCrop(
            Long cropId,
            Long farmId) {

        return cropRepository
                .findByCropIdAndFarmId(cropId, farmId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "CROP_NOT_FOUND",
                                        "Crop was not found"
                                )
                        )
                );
    }

    private void validateDates(CropRequest request) {

        if (!request.expectedHarvestDate()
                .isAfter(request.sowingDate())) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_CROP_DATES",
                    "Expected harvest date must be after sowing date"
            );
        }
    }

    private String normalize(String value) {

        return value.trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}