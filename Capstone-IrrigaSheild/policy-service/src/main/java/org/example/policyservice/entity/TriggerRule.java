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
@Table("trigger_rules")
public class TriggerRule {

    @Id
    @Column("trigger_rule_id")
    private Long triggerRuleId;

    @Column("plan_id")
    private Long planId;

    @Column("crop_type")
    private String cropType;

    @Column("growth_stage")
    private String growthStage;

    @Column("availability_threshold_pct")
    private BigDecimal availabilityThresholdPercentage;

    @Column("minimum_continuous_hours")
    private Integer minimumContinuousHours;

    @Column("rolling_window_days")
    private Integer rollingWindowDays;

    @Column("required_shortage_days")
    private Integer requiredShortageDays;

    @Column("status")
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}