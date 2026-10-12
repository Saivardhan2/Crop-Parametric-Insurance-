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
@Table("policy_applications")
public class PolicyApplication {

    @Id
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

    @Column("requested_coverage")
    private BigDecimal requestedCoverage;

    @Column("proposed_start_date")
    private LocalDate proposedStartDate;

    @Column("proposed_end_date")
    private LocalDate proposedEndDate;

    @Column("status")
    private String status;

    @Column("submitted_at")
    private LocalDateTime submittedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
