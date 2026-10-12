package org.example.policyservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.example.policyservice.dto.request.InsurancePlanRequest;
import org.example.policyservice.dto.request.PolicyApplicationRequest;
import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.Policy;
import org.example.policyservice.entity.PolicyApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyValidationAndEntityTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("PolicyApplicationRequest passes on valid data")
    void testPolicyApplicationRequestValid() {
        PolicyApplicationRequest req = new PolicyApplicationRequest(
                1L,
                2L,
                3L,
                new BigDecimal("25000.00"),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusMonths(6)
        );

        Set<ConstraintViolation<PolicyApplicationRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("PolicyApplicationRequest fails on null farmId")
    void testPolicyApplicationRequestNullFarm() {
        PolicyApplicationRequest req = new PolicyApplicationRequest(
                null,
                2L,
                3L,
                new BigDecimal("25000.00"),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusMonths(6)
        );

        Set<ConstraintViolation<PolicyApplicationRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("farmId"));
    }

    @Test
    @DisplayName("PolicyApplicationRequest fails when requested coverage is below 1000")
    void testPolicyApplicationRequestLowCoverage() {
        PolicyApplicationRequest req = new PolicyApplicationRequest(
                1L,
                2L,
                3L,
                new BigDecimal("500.00"),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusMonths(6)
        );

        Set<ConstraintViolation<PolicyApplicationRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("requestedCoverage"));
    }

    @Test
    @DisplayName("PolicyApplicationRequest fails when start date is in the past")
    void testPolicyApplicationRequestPastStartDate() {
        PolicyApplicationRequest req = new PolicyApplicationRequest(
                1L,
                2L,
                3L,
                new BigDecimal("25000.00"),
                LocalDate.now().minusDays(5),
                LocalDate.now().plusMonths(6)
        );

        Set<ConstraintViolation<PolicyApplicationRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("proposedStartDate"));
    }

    @Test
    @DisplayName("InsurancePlanRequest passes on valid data")
    void testInsurancePlanRequestValid() {
        InsurancePlanRequest req = new InsurancePlanRequest(
                "Irrigation Deficit Shield",
                "Protects against severe irrigation deficit",
                new BigDecimal("5000.00"),
                new BigDecimal("100000.00"),
                new BigDecimal("1500.00"),
                180
        );

        Set<ConstraintViolation<InsurancePlanRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("InsurancePlanRequest fails on blank plan name")
    void testInsurancePlanRequestBlankName() {
        InsurancePlanRequest req = new InsurancePlanRequest(
                "  ",
                "Description",
                new BigDecimal("5000.00"),
                new BigDecimal("100000.00"),
                new BigDecimal("1500.00"),
                180
        );

        Set<ConstraintViolation<InsurancePlanRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("planName"));
    }

    @Test
    @DisplayName("InsurancePlanRequest fails when base premium is zero or negative")
    void testInsurancePlanRequestZeroPremium() {
        InsurancePlanRequest req = new InsurancePlanRequest(
                "Test Plan",
                "Description",
                new BigDecimal("5000.00"),
                new BigDecimal("100000.00"),
                new BigDecimal("0.50"), // minimum is 1.00
                180
        );

        Set<ConstraintViolation<InsurancePlanRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("basePremium"));
    }

    @Test
    @DisplayName("InsurancePlan entity builder and mutators function properly")
    void testInsurancePlanEntity() {
        LocalDateTime now = LocalDateTime.now();
        InsurancePlan plan = InsurancePlan.builder()
                .planId(5L)
                .planName("Pro Shield")
                .description("Comprehensive coverage")
                .minimumCoverage(new BigDecimal("10000.00"))
                .maximumCoverage(new BigDecimal("200000.00"))
                .basePremium(new BigDecimal("2500.00"))
                .maximumPolicyDays(365)
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(plan.getPlanId()).isEqualTo(5L);
        assertThat(plan.getPlanName()).isEqualTo("Pro Shield");
        assertThat(plan.getBasePremium()).isEqualByComparingTo("2500.00");

        plan.setStatus("INACTIVE");
        assertThat(plan.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("PolicyApplication entity builder and mutators function properly")
    void testPolicyApplicationEntity() {
        LocalDate today = LocalDate.now();
        PolicyApplication app = PolicyApplication.builder()
                .applicationId(12L)
                .farmerUserId(7L)
                .farmerEmail("farmer7@test.com")
                .farmId(1L)
                .cropId(2L)
                .planId(3L)
                .requestedCoverage(new BigDecimal("30000.00"))
                .proposedStartDate(today)
                .proposedEndDate(today.plusMonths(4))
                .status("PENDING_REVIEW")
                .build();

        assertThat(app.getApplicationId()).isEqualTo(12L);
        assertThat(app.getFarmerUserId()).isEqualTo(7L);
        assertThat(app.getStatus()).isEqualTo("PENDING_REVIEW");

        app.setStatus("APPROVED");
        assertThat(app.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("Policy entity builder and mutators function properly")
    void testPolicyEntity() {
        LocalDate today = LocalDate.now();
        Policy policy = Policy.builder()
                .policyId(101L)
                .applicationId(12L)
                .farmerUserId(7L)
                .coverageAmount(new BigDecimal("30000.00"))
                .premiumAmount(new BigDecimal("1800.00"))
                .startDate(today)
                .endDate(today.plusMonths(4))
                .paymentStatus("PAID")
                .status("ACTIVE")
                .build();

        assertThat(policy.getPolicyId()).isEqualTo(101L);
        assertThat(policy.getPaymentStatus()).isEqualTo("PAID");
        assertThat(policy.getStatus()).isEqualTo("ACTIVE");
    }
}
