package org.example.farmservice.service;

import java.util.Collections;

import org.example.farmservice.dto.response
        .FarmRiskProfileResponse;
import org.example.farmservice.entity.Farm;
import org.example.farmservice.exception.ApiException;
import org.example.farmservice.repository.CropRepository;
import org.example.farmservice.repository.FarmRepository;
import org.example.farmservice.repository
        .IrrigationProfileRepository;
import org.example.farmservice.util.CropMapper;
import org.example.farmservice.util.FarmMapper;
import org.example.farmservice.util
        .IrrigationProfileMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class FarmRiskProfileServiceImpl
        implements FarmRiskProfileService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FarmRiskProfileServiceImpl.class);

    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final IrrigationProfileRepository
            irrigationProfileRepository;

    public FarmRiskProfileServiceImpl(
            FarmRepository farmRepository,
            CropRepository cropRepository,
            IrrigationProfileRepository
                    irrigationProfileRepository) {

        this.farmRepository = farmRepository;
        this.cropRepository = cropRepository;
        this.irrigationProfileRepository =
                irrigationProfileRepository;
    }

    @Override
    public Mono<FarmRiskProfileResponse> getRiskProfile(
            Long farmId) {

        Mono<Farm> farmMono = farmRepository
                .findById(farmId)
                .switchIfEmpty(
                        Mono.error(
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "FARM_NOT_FOUND",
                                        "Farm was not found"
                                )
                        )
                );

        return farmMono.flatMap(farm -> {

            var cropsMono = cropRepository
                    .findAllByFarmIdOrderByCreatedAtDesc(
                            farmId
                    )
                    .map(CropMapper::toResponse)
                    .collectList();

            var irrigationMono =
                    irrigationProfileRepository
                            .findByFarmId(farmId)
                            .map(
                                    IrrigationProfileMapper
                                            ::toResponse
                            )
                            .switchIfEmpty(
                                    Mono.error(
                                            new ApiException(
                                                    HttpStatus.CONFLICT,
                                                    "IRRIGATION_PROFILE_REQUIRED",
                                                    "Complete the irrigation profile before underwriting"
                                            )
                                    )
                            );

            return Mono.zip(
                    cropsMono,
                    irrigationMono
            ).map(result ->
                    new FarmRiskProfileResponse(
                            FarmMapper.toResponse(farm),
                            result.getT1(),
                            result.getT2()
                    )
            );
        });
    }
}