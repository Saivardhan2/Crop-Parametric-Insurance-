package org.example.farmservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.farmservice.dto.response.FarmRiskProfileResponse;
import org.example.farmservice.entity.Crop;
import org.example.farmservice.entity.Farm;
import org.example.farmservice.entity.IrrigationProfile;
import org.example.farmservice.exception.ApiException;
import org.example.farmservice.repository.CropRepository;
import org.example.farmservice.repository.FarmRepository;
import org.example.farmservice.repository.IrrigationProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmRiskProfileServiceImplExtendedTest {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private CropRepository cropRepository;

    @Mock
    private IrrigationProfileRepository irrigationProfileRepository;

    private FarmRiskProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FarmRiskProfileServiceImpl(farmRepository, cropRepository, irrigationProfileRepository);
    }

    @Test
    @DisplayName("getRiskProfile should return combined profile when all entities exist")
    void testGetRiskProfileSuccess() {
        Farm farm = Farm.builder()
                .farmId(1L)
                .farmerId(10L)
                .farmName("Green Acres")
                .surveyNumber("SN-123")
                .addressLine("Line 1")
                .village("Village A")
                .district("District B")
                .state("State C")
                .postalCode("123456")
                .areaInAcres(new BigDecimal("10.5"))
                .soilType("Loamy")
                .ownershipType("OWNED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Crop crop = Crop.builder()
                .cropId(101L)
                .farmId(1L)
                .cropType("Wheat")
                .variety("Sharbati")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        IrrigationProfile irrigation = IrrigationProfile.builder()
                .irrigationId(201L)
                .farmId(1L)
                .waterSource("BOREWELL")
                .irrigationMethod("DRIP")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(farmRepository.findById(1L)).thenReturn(Mono.just(farm));
        when(cropRepository.findAllByFarmIdOrderByCreatedAtDesc(1L)).thenReturn(Flux.just(crop));
        when(irrigationProfileRepository.findByFarmId(1L)).thenReturn(Mono.just(irrigation));

        StepVerifier.create(service.getRiskProfile(1L))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.farm().farmId()).isEqualTo(1L);
                    assertThat(response.crops()).hasSize(1);
                    assertThat(response.crops().get(0).cropType()).isEqualTo("Wheat");
                    assertThat(response.irrigationProfile().waterSource()).isEqualTo("BOREWELL");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getRiskProfile should fail with 404 when farm does not exist")
    void testGetRiskProfileFarmNotFound() {
        when(farmRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.getRiskProfile(99L))
                .expectErrorMatches(ex -> ex instanceof ApiException
                        && ((ApiException) ex).getStatus() == HttpStatus.NOT_FOUND
                        && ((ApiException) ex).getErrorCode().equals("FARM_NOT_FOUND"))
                .verify();
    }

    @Test
    @DisplayName("getRiskProfile should fail with 409 when irrigation profile is missing")
    void testGetRiskProfileMissingIrrigation() {
        Farm farm = Farm.builder()
                .farmId(1L)
                .farmerId(10L)
                .farmName("Green Acres")
                .areaInAcres(new BigDecimal("10.5"))
                .soilType("Loamy")
                .ownershipType("OWNED")
                .build();

        when(farmRepository.findById(1L)).thenReturn(Mono.just(farm));
        when(cropRepository.findAllByFarmIdOrderByCreatedAtDesc(1L)).thenReturn(Flux.empty());
        when(irrigationProfileRepository.findByFarmId(1L)).thenReturn(Mono.empty());

        StepVerifier.create(service.getRiskProfile(1L))
                .expectErrorMatches(ex -> ex instanceof ApiException
                        && ((ApiException) ex).getStatus() == HttpStatus.CONFLICT
                        && ((ApiException) ex).getErrorCode().equals("IRRIGATION_PROFILE_REQUIRED"))
                .verify();
    }

    @Test
    @DisplayName("getRiskProfile should succeed even when crops list is empty")
    void testGetRiskProfileWithEmptyCrops() {
        Farm farm = Farm.builder()
                .farmId(2L)
                .farmerId(10L)
                .farmName("New Farm")
                .areaInAcres(new BigDecimal("5.0"))
                .soilType("Clay")
                .ownershipType("OWNED")
                .build();

        IrrigationProfile irrigation = IrrigationProfile.builder()
                .irrigationId(202L)
                .farmId(2L)
                .waterSource("CANAL")
                .irrigationMethod("FLOOD")
                .build();

        when(farmRepository.findById(2L)).thenReturn(Mono.just(farm));
        when(cropRepository.findAllByFarmIdOrderByCreatedAtDesc(2L)).thenReturn(Flux.empty());
        when(irrigationProfileRepository.findByFarmId(2L)).thenReturn(Mono.just(irrigation));

        StepVerifier.create(service.getRiskProfile(2L))
                .assertNext(response -> {
                    assertThat(response.farm().farmId()).isEqualTo(2L);
                    assertThat(response.crops()).isEmpty();
                    assertThat(response.irrigationProfile().waterSource()).isEqualTo("CANAL");
                })
                .verifyComplete();
    }
}
