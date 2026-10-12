package org.example.eventclaimsservice.entity;

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
@Table("irrigation_readings")
public class IrrigationReading {

    @Id
    @Column("reading_id")
    private Long readingId;

    @Column("policy_id")
    private Long policyId;

    @Column("farm_id")
    private Long farmId;

    @Column("availability_percentage")
    private BigDecimal availabilityPercentage;

    @Column("recorded_at")
    private LocalDateTime recordedAt;

    @Column("submitted_by")
    private Long submittedBy;

    @Column("created_at")
    private LocalDateTime createdAt;
}