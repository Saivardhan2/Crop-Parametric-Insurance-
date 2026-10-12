package org.example.farmservice.entity;

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
@Table("farmer_profiles")
public class FarmerProfile {

    @Id
    @Column("farmer_id")
    private Long farmerId;

    @Column("auth_user_id")
    private Long authUserId;

    @Column("email")
    private String email;

    @Column("full_name")
    private String fullName;

    @Column("phone_number")
    private String phoneNumber;

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

    @Column("bank_account_last_four")
    private String bankAccountLastFour;

    @Column("verification_status")
    private String verificationStatus;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}