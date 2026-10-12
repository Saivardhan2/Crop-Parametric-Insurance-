package org.example.farmservice.entity;

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
@Table("farms")
public class Farm {

    @Id
    @Column("farm_id")
    private Long farmId;

    @Column("farmer_id")
    private Long farmerId;

    @Column("farm_name")
    private String farmName;

    @Column("survey_number")
    private String surveyNumber;

    @Column("address_line")
    private String addressLine;

    @Column("village")
    private String village;

    @Column("district")
    private String district;

    @Column("state")
    private String state;

    @Column("postal_code")
    private String postalCode;

    @Column("area_in_acres")
    private BigDecimal areaInAcres;

    @Column("soil_type")
    private String soilType;

    @Column("ownership_type")
    private String ownershipType;

    @Column("latitude")
    private BigDecimal latitude;

    @Column("longitude")
    private BigDecimal longitude;

    @Column("status")
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}