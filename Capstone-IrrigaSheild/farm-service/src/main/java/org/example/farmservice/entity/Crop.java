package org.example.farmservice.entity;

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
@Table("crops")
public class Crop {

    @Id
    @Column("crop_id")
    private Long cropId;

    @Column("farm_id")
    private Long farmId;

    @Column("crop_type")
    private String cropType;

    @Column("variety")
    private String variety;

    @Column("sowing_date")
    private LocalDate sowingDate;

    @Column("expected_harvest_date")
    private LocalDate expectedHarvestDate;

    @Column("growth_stage")
    private String growthStage;

    @Column("irrigation_requirement")
    private String irrigationRequirement;

    @Column("status")
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
