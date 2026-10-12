package org.example.farmservice.service;

import org.example.farmservice.dto.response
        .FarmApplicationValidationResponse;
import org.example.farmservice.exception.ApiException;
import org.example.farmservice.repository.CropRepository;
import org.example.farmservice.repository.FarmRepository;
import org.example.farmservice.repository
        .FarmerProfileRepository;
import org.example.farmservice.repository
        .IrrigationProfileRepository;
import org.example.farmservice.security.AuthenticatedUser;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class FarmApplicationValidationServiceImpl
        implements FarmApplicationValidationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FarmApplicationValidationServiceImpl.class);

    private final FarmerProfileRepository farmerProfileRepository;
    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final IrrigationProfileRepository
            irrigationProfileRepository;

    public FarmApplicationValidationServiceImpl(
            FarmerProfileRepository farmerProfileRepository,
            FarmRepository farmRepository,
            CropRepository cropRepository,
            IrrigationProfileRepository irrigationProfileRepository) {

        this.farmerProfileRepository = farmerProfileRepository;
        this.farmRepository = farmRepository;
        this.cropRepository = cropRepository;
        this.irrigationProfileRepository =
                irrigationProfileRepository;
    }

    @Override
    public Mono<FarmApplicationValidationResponse>
    validateForApplication(
            AuthenticatedUser user,
            Long farmId,
            Long cropId) {

        return farmerProfileRepository
                .findByAuthUserId(user.userId())
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
                        farmRepository
                                .findByFarmIdAndFarmerId(
                                        farmId,
                                        profile.getFarmerId()
                                )
                                .switchIfEmpty(
                                        Mono.error(
                                                new ApiException(
                                                        HttpStatus.NOT_FOUND,
                                                        "FARM_NOT_FOUND",
                                                        "Farm was not found"
                                                )
                                        )
                                )
                                .flatMap(farm ->
                                        cropRepository
                                                .findByCropIdAndFarmId(
                                                        cropId,
                                                        farmId
                                                )
                                                .switchIfEmpty(
                                                        Mono.error(
                                                                new ApiException(
                                                                        HttpStatus.NOT_FOUND,
                                                                        "CROP_NOT_FOUND",
                                                                        "Crop was not found for the selected farm"
                                                                )
                                                        )
                                                )
                                                .flatMap(crop ->
                                                        irrigationProfileRepository
                                                                .existsByFarmId(
                                                                        farmId
                                                                )
                                                                .flatMap(
                                                                        irrigationExists -> {

                                                                            if (!irrigationExists) {
                                                                                return Mono.error(
                                                                                        new ApiException(
                                                                                                HttpStatus.CONFLICT,
                                                                                                "IRRIGATION_PROFILE_REQUIRED",
                                                                                                "Complete the irrigation profile before applying"
                                                                                        )
                                                                                );
                                                                            }

                                                                            return Mono.just(
                                                                                    new FarmApplicationValidationResponse(
                                                                                            farm.getFarmId(),
                                                                                            crop.getCropId(),
                                                                                            profile.getFarmerId(),
                                                                                            farm.getFarmName(),
                                                                                            crop.getCropType(),
                                                                                            crop.getGrowthStage(),
                                                                                            true,
                                                                                            true
                                                                                    )
                                                                            );
                                                                        }
                                                                )
                                                )
                                )
                );
    }
}