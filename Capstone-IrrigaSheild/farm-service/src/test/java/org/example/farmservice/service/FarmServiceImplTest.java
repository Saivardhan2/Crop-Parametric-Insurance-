package org.example.farmservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FarmServiceImplTest {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private FarmerProfileRepository farmerProfileRepository;

    @Mock
    private FarmRequest farmRequest;

    private FarmServiceImpl farmService;
    private AuthenticatedUser farmer;

    @BeforeEach
    void setUp() {

        farmService = new FarmServiceImpl(
                farmRepository,
                farmerProfileRepository
        );

        farmer = new AuthenticatedUser(
                2L,
                "farmer@example.com"
        );
    }

    @Test
    void shouldCreateFarm() {

        mockFarmerProfile();
        mockFarmRequest();

        when(farmRepository
                .existsByFarmerIdAndFarmNameIgnoreCase(
                        1L,
                        "Green Farm"
                ))
                .thenReturn(Mono.just(false));

        when(farmRepository.save(any(Farm.class)))
                .thenAnswer(invocation -> {

                    Farm farm = invocation.getArgument(0);
                    farm.setFarmId(1L);

                    return Mono.just(farm);
                });

        StepVerifier.create(
                        farmService.createFarm(
                                farmer,
                                farmRequest
                        )
                )
                .assertNext(response -> {
                    assertEquals(1L, response.farmId());
                    assertEquals("Green Farm", response.farmName());
                    assertEquals("LOAMY", response.soilType());
                    assertEquals("OWNED", response.ownershipType());
                    assertEquals("ACTIVE", response.status());
                })
                .verifyComplete();

        ArgumentCaptor<Farm> farmCaptor =
                ArgumentCaptor.forClass(Farm.class);

        verify(farmRepository).save(
                farmCaptor.capture()
        );

        Farm savedFarm = farmCaptor.getValue();

        assertEquals(1L, savedFarm.getFarmerId());
        assertEquals("Green Farm", savedFarm.getFarmName());
        assertEquals("LOAMY", savedFarm.getSoilType());
        assertEquals("OWNED", savedFarm.getOwnershipType());
    }

    @Test
    void shouldRejectDuplicateFarmName() {

        mockFarmerProfile();

        when(farmRequest.farmName())
                .thenReturn("Green Farm");

        when(farmRepository
                .existsByFarmerIdAndFarmNameIgnoreCase(
                        1L,
                        "Green Farm"
                ))
                .thenReturn(Mono.just(true));

        StepVerifier.create(
                        farmService.createFarm(
                                farmer,
                                farmRequest
                        )
                )
                .expectErrorSatisfies(error -> {

                    assertTrue(error instanceof ApiException);

                    ApiException exception =
                            (ApiException) error;

                    assertEquals(
                            "FARM_ALREADY_EXISTS",
                            exception.getErrorCode()
                    );

                    assertEquals(
                            "A farm with this name already exists",
                            exception.getMessage()
                    );
                })
                .verify();

        verify(farmRepository, never())
                .save(any(Farm.class));
    }

    @Test
    void shouldReturnFarmerFarms() {

        mockFarmerProfile();

        Farm farm = createFarm();

        when(farmRepository
                .findAllByFarmerIdOrderByCreatedAtDesc(1L))
                .thenReturn(Flux.just(farm));

        StepVerifier.create(
                        farmService.getMyFarms(farmer)
                )
                .assertNext(response -> {
                    assertEquals(1L, response.farmId());
                    assertEquals("Green Farm", response.farmName());
                    assertEquals("ACTIVE", response.status());
                })
                .verifyComplete();
    }

    private void mockFarmerProfile() {

        FarmerProfile profile =
                new FarmerProfile();

        profile.setFarmerId(1L);

        when(farmerProfileRepository
                .findByAuthUserId(2L))
                .thenReturn(Mono.just(profile));
    }

    private void mockFarmRequest() {

        when(farmRequest.farmName())
                .thenReturn(" Green Farm ");

        when(farmRequest.surveyNumber())
                .thenReturn(" SURVEY-101 ");

        when(farmRequest.addressLine())
                .thenReturn(" Main Road ");

        when(farmRequest.village())
                .thenReturn(" Green Village ");

        when(farmRequest.district())
                .thenReturn(" Pune ");

        when(farmRequest.state())
                .thenReturn(" Maharashtra ");

        when(farmRequest.postalCode())
                .thenReturn("411001");

        when(farmRequest.areaInAcres())
                .thenReturn(new BigDecimal("5.50"));

        when(farmRequest.soilType())
                .thenReturn("loamy");

        when(farmRequest.ownershipType())
                .thenReturn("owned");

        when(farmRequest.latitude())
                .thenReturn(new BigDecimal("18.5204"));

        when(farmRequest.longitude())
                .thenReturn(new BigDecimal("73.8567"));
    }

    private Farm createFarm() {

        LocalDateTime now = LocalDateTime.now();

        return Farm.builder()
                .farmId(1L)
                .farmerId(1L)
                .farmName("Green Farm")
                .surveyNumber("SURVEY-101")
                .addressLine("Main Road")
                .village("Green Village")
                .district("Pune")
                .state("Maharashtra")
                .postalCode("411001")
                .areaInAcres(
                        new BigDecimal("5.50")
                )
                .soilType("LOAMY")
                .ownershipType("OWNED")
                .latitude(
                        new BigDecimal("18.5204")
                )
                .longitude(
                        new BigDecimal("73.8567")
                )
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}