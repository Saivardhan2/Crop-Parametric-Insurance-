package org.example.policyservice.underwriting.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.PolicyApplication;
import org.example.policyservice.exception.ApiException;
import org.example.policyservice.underwriting.dto.response.FarmRiskProfileResponse;
import org.example.policyservice.underwriting.dto.response.FarmRiskProfileResponse.CropDetails;
import org.example.policyservice.underwriting.dto.response.FarmRiskProfileResponse.FarmDetails;
import org.example.policyservice.underwriting.dto.response.FarmRiskProfileResponse.IrrigationDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskCalculatorComprehensiveTest {

    private final RiskCalculator calculator = new RiskCalculator();

    private InsurancePlan createPlan(BigDecimal basePrem, BigDecimal minCov, BigDecimal maxCov) {
        return InsurancePlan.builder()
                .planId(1L)
                .planName("Standard Plan")
                .basePremium(basePrem)
                .minimumCoverage(minCov)
                .maximumCoverage(maxCov)
                .build();
    }

    private PolicyApplication createApp(Long cropId, BigDecimal requestedCov) {
        return PolicyApplication.builder()
                .applicationId(10L)
                .cropId(cropId)
                .requestedCoverage(requestedCov)
                .build();
    }

    private FarmRiskProfileResponse createProfile(Long cropId, BigDecimal availability, String eqCond, Integer failCount, String seasonalRisk, String backupSource) {
        FarmDetails farm = new FarmDetails(1L, 2L, "Farm", "SN", "Addr", "Vill", "Dist", "State", "123456", new BigDecimal("10"), "Loam", "OWNED", null, null, "ACTIVE", LocalDateTime.now(), LocalDateTime.now());
        CropDetails crop = new CropDetails(cropId, 1L, "Rice", "Basmati", LocalDate.now(), LocalDate.now().plusMonths(3), "SOWING", "HIGH", "ACTIVE", LocalDateTime.now(), LocalDateTime.now());
        IrrigationDetails irr = new IrrigationDetails(1L, 1L, "CANAL", "DRIP", eqCond, backupSource, failCount, availability, seasonalRisk, "MONTHLY", LocalDate.now(), "ACTIVE", LocalDateTime.now(), LocalDateTime.now());
        return new FarmRiskProfileResponse(farm, List.of(crop), irr);
    }

    @Test
    @DisplayName("assess should classify as LOW risk and apply 5% loading")
    void testLowRiskAssessment() {
        InsurancePlan plan = createPlan(new BigDecimal("1000.00"), new BigDecimal("5000.00"), new BigDecimal("50000.00"));
        PolicyApplication app = createApp(100L, new BigDecimal("20000.00"));
        FarmRiskProfileResponse profile = createProfile(100L, new BigDecimal("90.0"), "GOOD", 0, "LOW", "BOREWELL");

        AssessmentResult result = calculator.assess(plan, app, profile);

        assertThat(result.riskLevel()).isEqualTo("LOW");
        assertThat(result.overallRiskScore()).isLessThanOrEqualTo(25);
        assertThat(result.riskLoading()).isEqualByComparingTo("50.00");
        assertThat(result.recommendedPremium()).isEqualByComparingTo("1050.00");
    }

    @Test
    @DisplayName("assess should classify as MODERATE risk and apply 15% loading")
    void testModerateRiskAssessment() {
        InsurancePlan plan = createPlan(new BigDecimal("1000.00"), new BigDecimal("5000.00"), new BigDecimal("50000.00"));
        PolicyApplication app = createApp(100L, new BigDecimal("20000.00"));
        FarmRiskProfileResponse profile = createProfile(100L, new BigDecimal("75.0"), "FAIR", 1, "MEDIUM", "BOREWELL");

        AssessmentResult result = calculator.assess(plan, app, profile);

        assertThat(result.riskLevel()).isEqualTo("MODERATE");
        assertThat(result.riskLoading()).isEqualByComparingTo("150.00");
        assertThat(result.recommendedPremium()).isEqualByComparingTo("1150.00");
    }

    @Test
    @DisplayName("assess should classify as HIGH risk and apply 30% loading")
    void testHighRiskAssessment() {
        InsurancePlan plan = createPlan(new BigDecimal("1000.00"), new BigDecimal("5000.00"), new BigDecimal("50000.00"));
        PolicyApplication app = createApp(100L, new BigDecimal("20000.00"));
        FarmRiskProfileResponse profile = createProfile(100L, new BigDecimal("55.0"), "POOR", 2, "HIGH", "NONE");

        AssessmentResult result = calculator.assess(plan, app, profile);

        assertThat(result.riskLevel()).isEqualTo("HIGH");
        assertThat(result.riskLoading()).isEqualByComparingTo("300.00");
        assertThat(result.recommendedPremium()).isEqualByComparingTo("1300.00");
    }

    @Test
    @DisplayName("assess should classify as VERY_HIGH risk and apply 50% loading")
    void testVeryHighRiskAssessment() {
        InsurancePlan plan = createPlan(new BigDecimal("2000.00"), new BigDecimal("5000.00"), new BigDecimal("50000.00"));
        PolicyApplication app = createApp(100L, new BigDecimal("20000.00"));
        FarmRiskProfileResponse profile = createProfile(100L, new BigDecimal("30.0"), "POOR", 5, "VERY_HIGH", "NONE");

        AssessmentResult result = calculator.assess(plan, app, profile);

        assertThat(result.riskLevel()).isEqualTo("VERY_HIGH");
        assertThat(result.riskLoading()).isEqualByComparingTo("1000.00");
        assertThat(result.recommendedPremium()).isEqualByComparingTo("3000.00");
    }

    @Test
    @DisplayName("assess should throw 409 when irrigation profile is null")
    void testMissingIrrigationProfileThrows() {
        InsurancePlan plan = createPlan(new BigDecimal("1000.00"), new BigDecimal("5000.00"), new BigDecimal("50000.00"));
        PolicyApplication app = createApp(100L, new BigDecimal("20000.00"));
        FarmRiskProfileResponse profile = new FarmRiskProfileResponse(null, List.of(), null);

        assertThatThrownBy(() -> calculator.assess(plan, app, profile))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasFieldOrPropertyWithValue("errorCode", "IRRIGATION_PROFILE_REQUIRED");
    }

    @Test
    @DisplayName("assess should throw 404 when crop is not part of farm crops")
    void testSelectedCropNotFoundThrows() {
        InsurancePlan plan = createPlan(new BigDecimal("1000.00"), new BigDecimal("5000.00"), new BigDecimal("50000.00"));
        PolicyApplication app = createApp(999L, new BigDecimal("20000.00")); // crop 999 does not match 100
        FarmRiskProfileResponse profile = createProfile(100L, new BigDecimal("90.0"), "GOOD", 0, "LOW", "BOREWELL");

        assertThatThrownBy(() -> calculator.assess(plan, app, profile))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasFieldOrPropertyWithValue("errorCode", "CROP_NOT_FOUND");
    }

    @Test
    @DisplayName("assess should clamp requested coverage to maximum coverage if exceeded")
    void testCoverageClampedToMaximum() {
        InsurancePlan plan = createPlan(new BigDecimal("1000.00"), new BigDecimal("5000.00"), new BigDecimal("50000.00"));
        PolicyApplication app = createApp(100L, new BigDecimal("90000.00")); // exceeds max 50000
        FarmRiskProfileResponse profile = createProfile(100L, new BigDecimal("90.0"), "GOOD", 0, "LOW", "BOREWELL");

        AssessmentResult result = calculator.assess(plan, app, profile);

        assertThat(result.recommendedCoverage()).isEqualByComparingTo("50000.00");
    }

    @Test
    @DisplayName("assess should clamp requested coverage to minimum coverage if below")
    void testCoverageClampedToMinimum() {
        InsurancePlan plan = createPlan(new BigDecimal("1000.00"), new BigDecimal("5000.00"), new BigDecimal("50000.00"));
        PolicyApplication app = createApp(100L, new BigDecimal("1000.00")); // below min 5000
        FarmRiskProfileResponse profile = createProfile(100L, new BigDecimal("90.0"), "GOOD", 0, "LOW", "BOREWELL");

        AssessmentResult result = calculator.assess(plan, app, profile);

        assertThat(result.recommendedCoverage()).isEqualByComparingTo("50000.00".substring(0, 4) + ".00");
        assertThat(result.recommendedCoverage()).isEqualByComparingTo("5000.00");
    }
}
