package org.example.policyservice.entity;

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
@Table("policies")
public class Policy {

    @Id
    @Column("policy_id")
    private Long policyId;

    @Column("application_id")
    private Long applicationId;

    @Column("farmer_user_id")
    private Long farmerUserId;

    @Column("farmer_email")
    private String farmerEmail;

    @Column("farm_id")
    private Long farmId;

    @Column("crop_id")
    private Long cropId;

    @Column("plan_id")
    private Long planId;

    @Column("coverage_amount")
    private BigDecimal coverageAmount;

    @Column("premium_amount")
    private BigDecimal premiumAmount;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;

    @Column("payment_status")
    private String paymentStatus;

    @Column("status")
    private String status;

    @Column("accepted_at")
    private LocalDateTime acceptedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}