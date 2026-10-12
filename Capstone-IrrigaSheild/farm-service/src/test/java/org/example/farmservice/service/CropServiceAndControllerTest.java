package org.example.farmservice.crop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import org.example.farmservice.controller.CropController;
import org.example.farmservice.dto.request.CropRequest;
import org.example.farmservice.dto.response.CropResponse;
import org.example.farmservice.entity.Crop;
import org.example.farmservice.entity.Farm;
import org.example.farmservice.entity.FarmerProfile;
import org.example.farmservice.repository.CropRepository;
import org.example.farmservice.repository.FarmRepository;
import org.example.farmservice.repository.FarmerProfileRepository;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.service.CropService;
import org.example.farmservice.service.CropServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CropServiceAndControllerTest {

    private CropRepository cropRepository;
    private FarmRepository farmRepository;
    private FarmerProfileRepository profileRepository;
    private CropServiceImpl cropService;
    private AuthenticatedUser user;
    private FarmerProfile profile;
    private Farm farm;

    @BeforeEach
    void setUp() {
        cropRepository = mock(CropRepository.class);
        farmRepository = mock(FarmRepository.class);
        profileRepository = mock(FarmerProfileRepository.class);
        cropService = new CropServiceImpl(cropRepository, farmRepository, profileRepository);

        user = new AuthenticatedUser(10L, "farmer@irrigashield.com");
        profile = FarmerProfile.builder().farmerId(1L).authUserId(10L).build();
        farm = Farm.builder().farmId(100L).farmerId(1L).farmName("Green Acres").build();
    }

    @Test
    void shouldCreateCropSuccessfully() {
        CropRequest request = new CropRequest("WHEAT", "HD-2967", LocalDate.now(), LocalDate.now().plusMonths(4), "SOWING", "MEDIUM");
        when(profileRepository.findByAuthUserId(10L)).thenReturn(Mono.just(profile));
        when(farmRepository.findByFarmIdAndFarmerId(100L, 1L)).thenReturn(Mono.just(farm));

        Crop savedCrop = Crop.builder()
                .cropId(5L)
                .farmId(100L)
                .cropType("WHEAT")
                .variety("HD-2967")
                .sowingDate(request.sowingDate())
                .expectedHarvestDate(request.expectedHarvestDate())
                .growthStage("SOWING")
                .irrigationRequirement("MEDIUM")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        when(cropRepository.save(any(Crop.class))).thenReturn(Mono.just(savedCrop));

        StepVerifier.create(cropService.createCrop(user, 100L, request))
                .assertNext(res -> {
                    assertThat(res.cropId()).isEqualTo(5L);
                    assertThat(res.cropType()).isEqualTo("WHEAT");
                })
                .verifyComplete();
    }

    @Test
    void shouldGetCropsForFarm() {
        when(profileRepository.findByAuthUserId(10L)).thenReturn(Mono.just(profile));
        when(farmRepository.findByFarmIdAndFarmerId(100L, 1L)).thenReturn(Mono.just(farm));

        Crop crop1 = Crop.builder().cropId(1L).farmId(100L).cropType("RICE").status("ACTIVE").build();
        when(cropRepository.findAllByFarmIdOrderByCreatedAtDesc(100L)).thenReturn(Flux.just(crop1));

        StepVerifier.create(cropService.getCrops(user, 100L))
                .assertNext(res -> assertThat(res.cropType()).isEqualTo("RICE"))
                .verifyComplete();
    }

    @Test
    void shouldGetCropById() {
        when(profileRepository.findByAuthUserId(10L)).thenReturn(Mono.just(profile));
        when(farmRepository.findByFarmIdAndFarmerId(100L, 1L)).thenReturn(Mono.just(farm));

        Crop crop1 = Crop.builder().cropId(2L).farmId(100L).cropType("MAIZE").status("ACTIVE").build();
        when(cropRepository.findByCropIdAndFarmId(2L, 100L)).thenReturn(Mono.just(crop1));

        StepVerifier.create(cropService.getCrop(user, 100L, 2L))
                .assertNext(res -> assertThat(res.cropType()).isEqualTo("MAIZE"))
                .verifyComplete();
    }

    @Test
    void controllerShouldDelegateToService() {
        CropService mockService = mock(CropService.class);
        CropController controller = new CropController(mockService);

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        CropResponse response = new CropResponse(1L, 100L, "WHEAT", "V1", LocalDate.now(), LocalDate.now().plusMonths(3), "SOWING", "MEDIUM", "ACTIVE", LocalDateTime.now(), LocalDateTime.now());

        when(mockService.getCrops(user, 100L)).thenReturn(Flux.just(response));

        StepVerifier.create(controller.getCrops(auth, 100L))
                .assertNext(res -> assertThat(res.cropType()).isEqualTo("WHEAT"))
                .verifyComplete();
    }
}
