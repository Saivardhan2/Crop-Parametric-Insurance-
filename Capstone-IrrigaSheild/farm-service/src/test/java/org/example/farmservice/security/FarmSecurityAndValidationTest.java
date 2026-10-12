package org.example.farmservice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.farmservice.dto.response.CropResponse;
import org.example.farmservice.dto.response.FarmResponse;
import org.example.farmservice.dto.response.FarmerProfileResponse;
import org.example.farmservice.dto.response.IrrigationProfileResponse;
import org.example.farmservice.entity.Crop;
import org.example.farmservice.entity.Farm;
import org.example.farmservice.entity.FarmerProfile;
import org.example.farmservice.entity.IrrigationProfile;
import org.example.farmservice.repository.CropRepository;
import org.example.farmservice.repository.FarmRepository;
import org.example.farmservice.repository.FarmerProfileRepository;
import org.example.farmservice.repository.IrrigationProfileRepository;
import org.example.farmservice.service.FarmApplicationValidationServiceImpl;
import org.example.farmservice.util.CropMapper;
import org.example.farmservice.util.FarmMapper;
import org.example.farmservice.util.FarmerProfileMapper;
import org.example.farmservice.util.IrrigationProfileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class FarmSecurityAndValidationTest {

    private FarmerProfileRepository farmerRepo;
    private FarmRepository farmRepo;
    private CropRepository cropRepo;
    private IrrigationProfileRepository irrigationRepo;
    private FarmApplicationValidationServiceImpl validationService;

    @BeforeEach
    void setUp() {
        farmerRepo = mock(FarmerProfileRepository.class);
        farmRepo = mock(FarmRepository.class);
        cropRepo = mock(CropRepository.class);
        irrigationRepo = mock(IrrigationProfileRepository.class);
        validationService = new FarmApplicationValidationServiceImpl(farmerRepo, farmRepo, cropRepo, irrigationRepo);
    }

    @Test
    void shouldValidateFarmApplicationEligibility() {
        AuthenticatedUser user = new AuthenticatedUser(100L, "farmer@irrigashield.com");
        FarmerProfile profile = FarmerProfile.builder().farmerId(1L).authUserId(100L).build();
        Farm farm = Farm.builder().farmId(10L).farmerId(1L).farmName("Sunny Meadows").build();
        Crop crop = Crop.builder().cropId(20L).farmId(10L).cropType("SOYBEAN").growthStage("VEGETATIVE").build();
        IrrigationProfile irrigation = IrrigationProfile.builder().irrigationId(30L).farmId(10L).build();

        when(farmerRepo.findByAuthUserId(100L)).thenReturn(Mono.just(profile));
        when(farmRepo.findByFarmIdAndFarmerId(10L, 1L)).thenReturn(Mono.just(farm));
        when(cropRepo.findByCropIdAndFarmId(20L, 10L)).thenReturn(Mono.just(crop));
        when(irrigationRepo.existsByFarmId(10L)).thenReturn(Mono.just(true));

        StepVerifier.create(validationService.validateForApplication(user, 10L, 20L))
                .assertNext(res -> {
                    assertThat(res.eligible()).isTrue();
                    assertThat(res.farmName()).isEqualTo("Sunny Meadows");
                    assertThat(res.cropType()).isEqualTo("SOYBEAN");
                })
                .verifyComplete();
    }

    @Test
    void shouldAuthenticateViaTrustedHeaders() {
        TrustedHeaderAuthenticationFilter filter = new TrustedHeaderAuthenticationFilter();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/farms")
                .header("X-User-Id", "42")
                .header("X-User-Email", "test@test.com")
                .header("X-User-Roles", "ROLE_FARMER")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void shouldMapEntitiesToResponses() {
        Crop crop = Crop.builder().cropId(1L).farmId(2L).cropType("CORN").build();
        CropResponse cr = CropMapper.toResponse(crop);
        assertThat(cr.cropType()).isEqualTo("CORN");

        Farm farm = Farm.builder().farmId(2L).farmName("Farm A").areaInAcres(new BigDecimal("10.5")).build();
        FarmResponse fr = FarmMapper.toResponse(farm);
        assertThat(fr.farmName()).isEqualTo("Farm A");

        FarmerProfile fp = FarmerProfile.builder().farmerId(3L).fullName("Alice").build();
        FarmerProfileResponse fpr = FarmerProfileMapper.toResponse(fp);
        assertThat(fpr.fullName()).isEqualTo("Alice");

        IrrigationProfile ip = IrrigationProfile.builder().irrigationId(4L).waterSource("WELL").build();
        IrrigationProfileResponse ipr = IrrigationProfileMapper.toResponse(ip);
        assertThat(ipr.waterSource()).isEqualTo("WELL");
    }
}
