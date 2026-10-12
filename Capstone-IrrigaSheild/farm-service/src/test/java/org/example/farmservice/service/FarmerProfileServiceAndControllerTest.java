package org.example.farmservice.farmer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;

import org.example.farmservice.controller.FarmerProfileController;
import org.example.farmservice.dto.request.FarmerProfileRequest;
import org.example.farmservice.dto.response.FarmerProfileResponse;
import org.example.farmservice.entity.FarmerProfile;
import org.example.farmservice.repository.FarmerProfileRepository;
import org.example.farmservice.security.AuthenticatedUser;
import org.example.farmservice.service.FarmerProfileService;
import org.example.farmservice.service.FarmerProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class FarmerProfileServiceAndControllerTest {

    private FarmerProfileRepository repository;
    private FarmerProfileServiceImpl service;
    private AuthenticatedUser user;

    @BeforeEach
    void setUp() {
        repository = mock(FarmerProfileRepository.class);
        service = new FarmerProfileServiceImpl(repository);
        user = new AuthenticatedUser(20L, "farmer@farm.com");
    }

    @Test
    void shouldCreateFarmerProfile() {
        FarmerProfileRequest req = new FarmerProfileRequest("John Farmer", "9876543210", "123 Farm Rd", "Village A", "Dist B", "State C", "123456", "1234");
        when(repository.existsByAuthUserId(20L)).thenReturn(Mono.just(false));

        FarmerProfile saved = FarmerProfile.builder()
                .farmerId(1L)
                .authUserId(20L)
                .fullName("John Farmer")
                .phoneNumber("9876543210")
                .addressLine("123 Farm Rd")
                .village("Village A")
                .district("Dist B")
                .state("State C")
                .postalCode("123456")
                .bankAccountLastFour("1234")
                .verificationStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repository.save(any(FarmerProfile.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.createProfile(user, req))
                .assertNext(res -> {
                    assertThat(res.farmerId()).isEqualTo(1L);
                    assertThat(res.fullName()).isEqualTo("John Farmer");
                    assertThat(res.verificationStatus()).isEqualTo("PENDING");
                })
                .verifyComplete();
    }

    @Test
    void shouldGetMyProfile() {
        FarmerProfile profile = FarmerProfile.builder()
                .farmerId(1L)
                .authUserId(20L)
                .fullName("John Farmer")
                .phoneNumber("9876543210")
                .verificationStatus("VERIFIED")
                .createdAt(LocalDateTime.now())
                .build();
        when(repository.findByAuthUserId(20L)).thenReturn(Mono.just(profile));

        StepVerifier.create(service.getMyProfile(user))
                .assertNext(res -> assertThat(res.fullName()).isEqualTo("John Farmer"))
                .verifyComplete();
    }

    @Test
    void controllerShouldHandleProfileEndpoints() {
        FarmerProfileService mockService = mock(FarmerProfileService.class);
        FarmerProfileController controller = new FarmerProfileController(mockService);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());

        FarmerProfileResponse res = new FarmerProfileResponse(1L, 20L, "farmer@farm.com", "John Farmer", "9876543210", "Road", "Vill", "Dist", "State", "123456", "1234", "VERIFIED", LocalDateTime.now(), LocalDateTime.now());
        when(mockService.getMyProfile(user)).thenReturn(Mono.just(res));

        StepVerifier.create(controller.getMyProfile(auth))
                .assertNext(entity -> assertThat(entity.getBody().fullName()).isEqualTo("John Farmer"))
                .verifyComplete();
    }
}
