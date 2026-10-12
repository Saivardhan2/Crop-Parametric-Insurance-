package org.example.farmservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.farmservice.dto.request.FarmRequest;
import org.example.farmservice.entity.Farm;
import org.example.farmservice.entity.FarmerProfile;
import org.example.farmservice.exception.ApiException;
import org.example.farmservice.repository.FarmRepository;
import org.example.farmservice.repository.FarmerProfileRepository;
import org.example.farmservice.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmServiceImplExtendedTest {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private FarmerProfileRepository farmerProfileRepository;

    private FarmServiceImpl farmService;
    private final AuthenticatedUser user = new AuthenticatedUser(5L, "farmer@test.com");

    @BeforeEach
    void setUp() {
        farmService = new FarmServiceImpl(farmRepository, farmerProfileRepository);
    }

    @Test
    @DisplayName("getMyFarmById should return farm response when farmer owns the farm")
    void testGetMyFarmByIdSuccess() {
        FarmerProfile profile = FarmerProfile.builder().farmerId(100L).authUserId(5L).build();
        Farm farm = Farm.builder()
                .farmId(10L)
                .farmerId(100L)
                .farmName("Sunrise Farm")
                .areaInAcres(new BigDecimal("15.00"))
                .soilType("Loamy")
                .ownershipType("OWNED")
                .build();

        when(farmerProfileRepository.findByAuthUserId(5L)).thenReturn(Mono.just(profile));
        when(farmRepository.findByFarmIdAndFarmerId(10L, 100L)).thenReturn(Mono.just(farm));

        StepVerifier.create(farmService.getMyFarmById(user, 10L))
                .assertNext(res -> {
                    assertThat(res.farmId()).isEqualTo(10L);
                    assertThat(res.farmName()).isEqualTo("Sunrise Farm");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getMyFarmById should fail with 404 when farmer profile does not exist")
    void testGetMyFarmByIdNoProfile() {
        when(farmerProfileRepository.findByAuthUserId(5L)).thenReturn(Mono.empty());

        StepVerifier.create(farmService.getMyFarmById(user, 10L))
                .expectErrorMatches(ex -> ex instanceof ApiException
                        && ((ApiException) ex).getStatus() == HttpStatus.NOT_FOUND
                        && ((ApiException) ex).getErrorCode().equals("FARMER_PROFILE_NOT_FOUND"))
                .verify();
    }

    @Test
    @DisplayName("getMyFarmById should fail with 404 when farm is not found or not owned")
    void testGetMyFarmByIdNotOwned() {
        FarmerProfile profile = FarmerProfile.builder().farmerId(100L).authUserId(5L).build();

        when(farmerProfileRepository.findByAuthUserId(5L)).thenReturn(Mono.just(profile));
        when(farmRepository.findByFarmIdAndFarmerId(10L, 100L)).thenReturn(Mono.empty());

        StepVerifier.create(farmService.getMyFarmById(user, 10L))
                .expectErrorMatches(ex -> ex instanceof ApiException
                        && ((ApiException) ex).getStatus() == HttpStatus.NOT_FOUND
                        && ((ApiException) ex).getErrorCode().equals("FARM_NOT_FOUND"))
                .verify();
    }

    @Test
    @DisplayName("updateMyFarm should update details and save farm")
    void testUpdateMyFarmSuccess() {
        FarmerProfile profile = FarmerProfile.builder().farmerId(100L).authUserId(5L).build();
        Farm existing = Farm.builder()
                .farmId(10L)
                .farmerId(100L)
                .farmName("Old Name")
                .areaInAcres(new BigDecimal("15.00"))
                .soilType("Loamy")
                .ownershipType("OWNED")
                .build();

        FarmRequest updateReq = new FarmRequest(
                "New Name Farm",
                "SN-999",
                "456 New Road",
                "Village V",
                "District D",
                "State S",
                "500002",
                new BigDecimal("20.00"),
                "Clay",
                "LEASED",
                null,
                null
        );

        when(farmerProfileRepository.findByAuthUserId(5L)).thenReturn(Mono.just(profile));
        when(farmRepository.findByFarmIdAndFarmerId(10L, 100L)).thenReturn(Mono.just(existing));
        when(farmRepository.save(any(Farm.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(farmService.updateMyFarm(user, 10L, updateReq))
                .assertNext(res -> {
                    assertThat(res.farmName()).isEqualTo("New Name Farm");
                    assertThat(res.soilType()).isEqualTo("CLAY");
                })
                .verifyComplete();
    }
}
