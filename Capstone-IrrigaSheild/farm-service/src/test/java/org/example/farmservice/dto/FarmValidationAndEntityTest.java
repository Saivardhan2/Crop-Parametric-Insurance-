package org.example.farmservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.example.farmservice.dto.request.CropRequest;
import org.example.farmservice.dto.request.FarmRequest;
import org.example.farmservice.dto.request.FarmerProfileRequest;
import org.example.farmservice.entity.Farm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FarmValidationAndEntityTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("FarmRequest passes on valid inputs")
    void testFarmRequestValid() {
        FarmRequest req = new FarmRequest(
                "River Valley Farm",
                "SN-1024",
                "123 River Road",
                "Green Valley",
                "Pune",
                "Maharashtra",
                "411001",
                new BigDecimal("12.50"),
                "Clay",
                "OWNED",
                new BigDecimal("18.5204"),
                new BigDecimal("73.8567")
        );

        Set<ConstraintViolation<FarmRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("FarmRequest fails on blank farm name")
    void testFarmRequestBlankName() {
        FarmRequest req = new FarmRequest(
                " ",
                "SN-1024",
                "123 River Road",
                "Green Valley",
                "Pune",
                "Maharashtra",
                "411001",
                new BigDecimal("12.50"),
                "Clay",
                "OWNED",
                null,
                null
        );

        Set<ConstraintViolation<FarmRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("farmName"));
    }

    @Test
    @DisplayName("FarmRequest fails on invalid postal code")
    void testFarmRequestInvalidPostal() {
        FarmRequest req = new FarmRequest(
                "River Valley Farm",
                "SN-1024",
                "123 River Road",
                "Green Valley",
                "Pune",
                "Maharashtra",
                "011001",
                new BigDecimal("12.50"),
                "Clay",
                "OWNED",
                null,
                null
        );

        Set<ConstraintViolation<FarmRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("postalCode"));
    }

    @Test
    @DisplayName("FarmRequest fails on invalid ownership type")
    void testFarmRequestInvalidOwnership() {
        FarmRequest req = new FarmRequest(
                "River Valley Farm",
                "SN-1024",
                "123 River Road",
                "Green Valley",
                "Pune",
                "Maharashtra",
                "411001",
                new BigDecimal("12.50"),
                "Clay",
                "UNKNOWN_TYPE",
                null,
                null
        );

        Set<ConstraintViolation<FarmRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("ownershipType"));
    }

    @Test
    @DisplayName("CropRequest passes on valid inputs")
    void testCropRequestValid() {
        CropRequest req = new CropRequest(
                "Wheat",
                "Sharbati",
                LocalDate.now().minusDays(10),
                LocalDate.now().plusMonths(3),
                "VEGETATIVE",
                "MEDIUM"
        );

        Set<ConstraintViolation<CropRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CropRequest fails on invalid growth stage")
    void testCropRequestInvalidGrowthStage() {
        CropRequest req = new CropRequest(
                "Wheat",
                "Sharbati",
                LocalDate.now().minusDays(10),
                LocalDate.now().plusMonths(3),
                "DYING_STAGE",
                "MEDIUM"
        );

        Set<ConstraintViolation<CropRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("growthStage"));
    }

    @Test
    @DisplayName("CropRequest fails when sowing date is in the future")
    void testCropRequestFutureSowingDate() {
        CropRequest req = new CropRequest(
                "Wheat",
                "Sharbati",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusMonths(3),
                "SOWING",
                "LOW"
        );

        Set<ConstraintViolation<CropRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("sowingDate"));
    }

    @Test
    @DisplayName("FarmerProfileRequest passes on valid mobile and fields")
    void testFarmerProfileRequestValid() {
        FarmerProfileRequest req = new FarmerProfileRequest(
                "Ramesh Kumar",
                "9876543210",
                "Farm House 12",
                "Village X",
                "District Y",
                "State Z",
                "500001",
                "1234"
        );

        Set<ConstraintViolation<FarmerProfileRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("FarmerProfileRequest fails on invalid phone number")
    void testFarmerProfileRequestInvalidPhone() {
        FarmerProfileRequest req = new FarmerProfileRequest(
                "Ramesh Kumar",
                "12345",
                "Farm House 12",
                "Village X",
                "District Y",
                "State Z",
                "500001",
                "1234"
        );

        Set<ConstraintViolation<FarmerProfileRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber"));
    }

    @Test
    @DisplayName("Farm entity builder and getters/setters should function properly")
    void testFarmEntityBuilder() {
        LocalDateTime now = LocalDateTime.now();
        Farm farm = Farm.builder()
                .farmId(10L)
                .farmerId(2L)
                .farmName("Sunflower Fields")
                .areaInAcres(new BigDecimal("25.00"))
                .soilType("Loam")
                .ownershipType("LEASED")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(farm.getFarmId()).isEqualTo(10L);
        assertThat(farm.getFarmerId()).isEqualTo(2L);
        assertThat(farm.getFarmName()).isEqualTo("Sunflower Fields");
        assertThat(farm.getAreaInAcres()).isEqualByComparingTo("25.00");

        farm.setFarmName("Golden Fields");
        assertThat(farm.getFarmName()).isEqualTo("Golden Fields");
    }
}
