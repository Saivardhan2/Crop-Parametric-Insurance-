package org.example.policyservice.entity;

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
@Table("insurance_plans")
public class InsurancePlan {

    @Id
    @Column("plan_id")
    private Long planId;

    @Column("plan_name")
    private String planName;

    @Column("description")
    private String description;

    @Column("minimum_coverage")
    private BigDecimal minimumCoverage;

    @Column("maximum_coverage")
    private BigDecimal maximumCoverage;

    @Column("base_premium")
    private BigDecimal basePremium;

    @Column("maximum_policy_days")
    private Integer maximumPolicyDays;

    @Column("status")
    private String status;

    @Column("created_by")
    private Long createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}