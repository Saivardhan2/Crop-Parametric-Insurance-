package org.example.farmservice.irrigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.example.farmservice.controller.FarmRiskProfileController;
import org.example.farmservice.controller.IrrigationProfileController;
import org.example.farmservice.dto.request.IrrigationProfileRequest;
import org.example.farmservice.dto.response.FarmRiskProfileResponse;
import org.example.farmservice.dto.response.IrrigationProfileResponse;
import org.example.farmservice.entity.Farm;
import org.example.farmservice.entity.FarmerProfile;
import org.example.farmservice.entity.IrrigationProfile;
import org.example.farmservice.repository.FarmRepository;
import org.example.farmservice.repository.FarmerProfileRepository;
import org.example.farmservice.repository.IrrigationProfileRepository;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.service.FarmRiskProfileService;
import org.example.farmservice.service.IrrigationProfileService;
import org.example.farmservice.service.IrrigationProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class IrrigationProfileServiceAndControllerTest {

    private IrrigationProfileRepository irrigationRepository;
    private FarmRepository farmRepository;
    private FarmerProfileRepository farmerProfileRepository;
    private IrrigationProfileServiceImpl service;
    private AuthenticatedUser user;
    private FarmerProfile profile;
    private Farm farm;

    @BeforeEach
    void setUp() {
        irrigationRepository = mock(IrrigationProfileRepository.class);
        farmRepository = mock(FarmRepository.class);
        farmerProfileRepository = mock(FarmerProfileRepository.class);
        service = new IrrigationProfileServiceImpl(irrigationRepository, farmRepository, farmerProfileRepository);

        user = new AuthenticatedUser(30L, "farmer30@irrigashield.com");
        profile = FarmerProfile.builder().farmerId(2L).authUserId(30L).build();
        farm = Farm.builder().farmId(200L).farmerId(2L).build();
    }

    @Test
    void shouldCreateIrrigationProfile() {
        IrrigationProfileRequest req = new IrrigationProfileRequest(
                "BOREWELL", "DRIP", "GOOD", "CANAL", 0, new BigDecimal("85.00"), "LOW", "MONTHLY", LocalDate.now().minusMonths(1)
        );

        when(farmerProfileRepository.findByAuthUserId(30L)).thenReturn(Mono.just(profile));
        when(farmRepository.findByFarmIdAndFarmerId(200L, 2L)).thenReturn(Mono.just(farm));
        when(irrigationRepository.existsByFarmId(200L)).thenReturn(Mono.just(false));

        IrrigationProfile saved = IrrigationProfile.builder()
                .irrigationId(10L)
                .farmId(200L)
                .waterSource("BOREWELL")
                .irrigationMethod("DRIP")
                .equipmentCondition("GOOD")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        when(irrigationRepository.save(any(IrrigationProfile.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.createProfile(user, 200L, req))
                .assertNext(res -> {
                    assertThat(res.irrigationId()).isEqualTo(10L);
                    assertThat(res.waterSource()).isEqualTo("BOREWELL");
                })
                .verifyComplete();
    }

    @Test
    void shouldGetIrrigationProfile() {
        when(farmerProfileRepository.findByAuthUserId(30L)).thenReturn(Mono.just(profile));
        when(farmRepository.findByFarmIdAndFarmerId(200L, 2L)).thenReturn(Mono.just(farm));

        IrrigationProfile existing = IrrigationProfile.builder()
                .irrigationId(10L)
                .farmId(200L)
                .waterSource("BOREWELL")
                .status("ACTIVE")
                .build();
        when(irrigationRepository.findByFarmId(200L)).thenReturn(Mono.just(existing));

        StepVerifier.create(service.getProfile(user, 200L))
                .assertNext(res -> assertThat(res.irrigationId()).isEqualTo(10L))
                .verifyComplete();
    }

    @Test
    void controllerShouldHandleRiskProfile() {
        FarmRiskProfileService mockRiskService = mock(FarmRiskProfileService.class);
        FarmRiskProfileController controller = new FarmRiskProfileController(mockRiskService);

        FarmRiskProfileResponse res = new FarmRiskProfileResponse(null, List.of(), null);
        when(mockRiskService.getRiskProfile(200L)).thenReturn(Mono.just(res));

        StepVerifier.create(controller.getRiskProfile(200L))
                .assertNext(entity -> assertThat(entity.getBody()).isNotNull())
                .verifyComplete();
    }
}
