package org.example.eventclaimsservice.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table("claims")
public class Claim {

    @Id
    @Column("claim_id")
    private Long claimId;

    @Column("reading_id")
    private Long readingId;

    @Column("policy_id")
    private Long policyId;

    @Column("event_date")
    private LocalDate eventDate;

    @Column("farmer_user_id")
    private Long farmerUserId;

    @Column("farm_id")
    private Long farmId;

    @Column("availability_percentage")
    private BigDecimal availabilityPercentage;

    @Column("payout_percentage")
    private BigDecimal payoutPercentage;

    @Column("coverage_amount")
    private BigDecimal coverageAmount;

    @Column("compensation_amount")
    private BigDecimal compensationAmount;

    @Column("payout_status")
    private String payoutStatus;

    @Column("status")
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("settled_at")
    private LocalDateTime settledAt;
}
