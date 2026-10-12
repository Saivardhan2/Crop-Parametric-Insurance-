package org.example.farmservice.entity;

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
@Table("irrigation_profiles")
public class IrrigationProfile {

    @Id
    @Column("irrigation_id")
    private Long irrigationId;

    @Column("farm_id")
    private Long farmId;

    @Column("water_source")
    private String waterSource;

    @Column("irrigation_method")
    private String irrigationMethod;

    @Column("equipment_condition")
    private String equipmentCondition;

    @Column("backup_water_source")
    private String backupWaterSource;

    @Column("historical_failure_count")
    private Integer historicalFailureCount;

    @Column("average_water_availability_pct")
    private BigDecimal averageWaterAvailabilityPercentage;

    @Column("seasonal_risk")
    private String seasonalRisk;

    @Column("maintenance_frequency")
    private String maintenanceFrequency;

    @Column("last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column("status")
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}