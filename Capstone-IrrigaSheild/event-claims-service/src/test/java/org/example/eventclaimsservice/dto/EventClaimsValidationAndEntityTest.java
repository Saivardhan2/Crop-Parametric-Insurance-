package org.example.eventclaimsservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.example.eventclaimsservice.entity.Claim;
import org.example.eventclaimsservice.entity.IrrigationReading;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventClaimsValidationAndEntityTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("IrrigationReadingRequest passes on valid inputs")
    void testReadingRequestValid() {
        IrrigationReadingRequest req = new IrrigationReadingRequest(
                10L,
                new BigDecimal("45.50"),
                LocalDateTime.now()
        );

        Set<ConstraintViolation<IrrigationReadingRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("IrrigationReadingRequest fails on null policyId")
    void testReadingRequestNullPolicy() {
        IrrigationReadingRequest req = new IrrigationReadingRequest(
                null,
                new BigDecimal("45.50"),
                LocalDateTime.now()
        );

        Set<ConstraintViolation<IrrigationReadingRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("policyId"));
    }

    @Test
    @DisplayName("IrrigationReadingRequest fails when availability percentage exceeds 100")
    void testReadingRequestExceedsMax() {
        IrrigationReadingRequest req = new IrrigationReadingRequest(
                10L,
                new BigDecimal("105.00"),
                LocalDateTime.now()
        );

        Set<ConstraintViolation<IrrigationReadingRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("availabilityPercentage"));
    }

    @Test
    @DisplayName("IrrigationReadingRequest fails when availability percentage is negative")
    void testReadingRequestNegative() {
        IrrigationReadingRequest req = new IrrigationReadingRequest(
                10L,
                new BigDecimal("-5.00"),
                LocalDateTime.now()
        );

        Set<ConstraintViolation<IrrigationReadingRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("availabilityPercentage"));
    }

    @Test
    @DisplayName("Claim entity builder and getters/setters work properly")
    void testClaimEntity() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        Claim claim = Claim.builder()
                .claimId(101L)
                .readingId(201L)
                .policyId(1L)
                .eventDate(today)
                .farmerUserId(5L)
                .farmId(10L)
                .availabilityPercentage(new BigDecimal("30.00"))
                .payoutPercentage(new BigDecimal("50.00"))
                .coverageAmount(new BigDecimal("10000.00"))
                .compensationAmount(new BigDecimal("5000.00"))
                .payoutStatus("COMPLETED")
                .status("APPROVED")
                .createdAt(now)
                .settledAt(now)
                .build();

        assertThat(claim.getClaimId()).isEqualTo(101L);
        assertThat(claim.getCompensationAmount()).isEqualByComparingTo("5000.00");
        assertThat(claim.getStatus()).isEqualTo("APPROVED");

        claim.setStatus("REJECTED");
        assertThat(claim.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("IrrigationReading entity builder and getters/setters work properly")
    void testIrrigationReadingEntity() {
        LocalDateTime now = LocalDateTime.now();
        IrrigationReading reading = IrrigationReading.builder()
                .readingId(50L)
                .policyId(1L)
                .farmId(10L)
                .availabilityPercentage(new BigDecimal("25.00"))
                .recordedAt(now)
                .submittedBy(3L)
                .createdAt(now)
                .build();

        assertThat(reading.getReadingId()).isEqualTo(50L);
        assertThat(reading.getAvailabilityPercentage()).isEqualByComparingTo("25.00");
        assertThat(reading.getSubmittedBy()).isEqualTo(3L);
    }

    @Test
    @DisplayName("PayoutSlabConfig record holds correct values")
    void testPayoutSlabConfig() {
        PayoutSlabConfig slab = new PayoutSlabConfig(
                1L, 2L, 0, 24, new BigDecimal("0.0"), new BigDecimal("30.0"), new BigDecimal("75.0"), "ACTIVE"
        );

        assertThat(slab.payoutSlabId()).isEqualTo(1L);
        assertThat(slab.payoutPercentage()).isEqualByComparingTo("75.0");
        assertThat(slab.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("TriggerRuleConfig record holds correct values")
    void testTriggerRuleConfig() {
        TriggerRuleConfig rule = new TriggerRuleConfig(
                1L, 10L, "WHEAT", "VEGETATIVE", new BigDecimal("40.0"), 12, 7, 3, "ACTIVE"
        );

        assertThat(rule.triggerRuleId()).isEqualTo(1L);
        assertThat(rule.availabilityThresholdPercentage()).isEqualByComparingTo("40.0");
        assertThat(rule.cropType()).isEqualTo("WHEAT");
    }
}
