package org.example.policyservice.underwriting.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("underwriting_cases")
public class UnderwritingCase {

    @Id
    @Column("case_id")
    private Long caseId;

    @Column("application_id")
    private Long applicationId;

    @Column("underwriter_user_id")
    private Long underwriterUserId;

    @Column("underwriter_email")
    private String underwriterEmail;

    @Column("water_availability_score")
    private Integer waterAvailabilityScore;

    @Column("equipment_condition_score")
    private Integer equipmentConditionScore;

    @Column("historical_failure_score")
    private Integer historicalFailureScore;

    @Column("seasonal_risk_score")
    private Integer seasonalRiskScore;

    @Column("backup_source_score")
    private Integer backupSourceScore;

    @Column("overall_risk_score")
    private Integer overallRiskScore;

    @Column("risk_level")
    private String riskLevel;

    @Column("base_premium")
    private BigDecimal basePremium;

    @Column("risk_loading")
    private BigDecimal riskLoading;

    @Column("recommended_premium")
    private BigDecimal recommendedPremium;

    @Column("recommended_coverage")
    private BigDecimal recommendedCoverage;

    @Column("decision")
    private String decision;

    @Column("remarks")
    private String remarks;

    @Column("status")
    private String status;

    @Column("review_started_at")
    private LocalDateTime reviewStartedAt;

    @Column("review_completed_at")
    private LocalDateTime reviewCompletedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}