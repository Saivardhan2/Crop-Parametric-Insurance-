package org.example.policyservice.underwriting.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.policyservice.entity.InsurancePlan;
import org.example.policyservice.entity.PolicyApplication;
import org.example.policyservice.underwriting.dto.response
        .FarmRiskProfileResponse;
import org.example.policyservice.underwriting.dto.response
        .FarmRiskProfileResponse.CropDetails;
import org.example.policyservice.underwriting.dto.response
        .FarmRiskProfileResponse.IrrigationDetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RiskCalculatorTest {

    private RiskCalculator riskCalculator;
    private InsurancePlan plan;
    private PolicyApplication application;

    @BeforeEach
    void setUp() {

        riskCalculator = new RiskCalculator();

        plan = InsurancePlan.builder()
                .planId(1L)
                .basePremium(
                        new BigDecimal("2000.00")
                )
                .minimumCoverage(
                        new BigDecimal("10000.00")
                )
                .maximumCoverage(
                        new BigDecimal("100000.00")
                )
                .build();

        application = PolicyApplication.builder()
                .applicationId(1L)
                .farmId(1L)
                .cropId(1L)
                .planId(1L)
                .requestedCoverage(
                        new BigDecimal("60000.00")
                )
                .build();
    }

    @Test
    void shouldCalculateLowRisk() {

        FarmRiskProfileResponse riskProfile =
                createRiskProfile(
                        new BigDecimal("90.00"),
                        "GOOD",
                        0,
                        "LOW",
                        "BOREWELL"
                );

        AssessmentResult result =
                riskCalculator.assess(
                        plan,
                        application,
                        riskProfile
                );

        assertEquals(0, result.overallRiskScore());
        assertEquals("LOW", result.riskLevel());

        assertEquals(
                new BigDecimal("100.00"),
                result.riskLoading()
        );

        assertEquals(
                new BigDecimal("2100.00"),
                result.recommendedPremium()
        );

        assertEquals(
                new BigDecimal("60000.00"),
                result.recommendedCoverage()
        );
    }

    @Test
    void shouldCalculateHighRisk() {

        FarmRiskProfileResponse riskProfile =
                createRiskProfile(
                        new BigDecimal("40.00"),
                        "POOR",
                        4,
                        "HIGH",
                        "NONE"
                );

        AssessmentResult result =
                riskCalculator.assess(
                        plan,
                        application,
                        riskProfile
                );

        assertEquals(95, result.overallRiskScore());
        assertEquals("VERY_HIGH", result.riskLevel());

        assertEquals(
                new BigDecimal("1000.00"),
                result.riskLoading()
        );

        assertEquals(
                new BigDecimal("3000.00"),
                result.recommendedPremium()
        );
    }

    private FarmRiskProfileResponse createRiskProfile(
            BigDecimal availability,
            String equipmentCondition,
            Integer failureCount,
            String seasonalRisk,
            String backupSource) {

        CropDetails crop = new CropDetails(
                1L,
                1L,
                "COTTON",
                "BT COTTON",
                LocalDate.now().minusDays(30),
                LocalDate.now().plusDays(90),
                "VEGETATIVE",
                "HIGH",
                "ACTIVE",
                null,
                null
        );

        IrrigationDetails irrigation =
                new IrrigationDetails(
                        1L,
                        1L,
                        "BOREWELL",
                        "DRIP",
                        equipmentCondition,
                        backupSource,
                        failureCount,
                        availability,
                        seasonalRisk,
                        "MONTHLY",
                        LocalDate.now().minusDays(15),
                        "ACTIVE",
                        null,
                        null
                );

        return new FarmRiskProfileResponse(
                null,
                List.of(crop),
                irrigation
        );
    }
}