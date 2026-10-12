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
@Table("payout_slabs")
public class PayoutSlab {

    @Id
    @Column("payout_slab_id")
    private Long payoutSlabId;

    @Column("trigger_rule_id")
    private Long triggerRuleId;

    @Column("minimum_duration_hours")
    private Integer minimumDurationHours;

    @Column("maximum_duration_hours")
    private Integer maximumDurationHours;

    @Column("min_availability_pct")
    private BigDecimal minAvailabilityPct;

    @Column("max_availability_pct")
    private BigDecimal maxAvailabilityPct;

    @Column("payout_percentage")
    private BigDecimal payoutPercentage;

    @Column("status")
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}