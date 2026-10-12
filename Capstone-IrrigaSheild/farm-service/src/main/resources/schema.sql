CREATE TABLE IF NOT EXISTS farmer_profiles (
   farmer_id BIGINT NOT NULL AUTO_INCREMENT,
   auth_user_id BIGINT NOT NULL,
   email VARCHAR(150) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    address_line VARCHAR(200) NOT NULL,
    village VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    bank_account_last_four VARCHAR(4),
    verification_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_farmer_profiles
    PRIMARY KEY (farmer_id),

    CONSTRAINT uk_farmer_profiles_auth_user
    UNIQUE (auth_user_id),

    CONSTRAINT uk_farmer_profiles_email
    UNIQUE (email),

    INDEX idx_farmer_profiles_district (district),

    INDEX idx_farmer_profiles_verification_status
(verification_status)
    );


CREATE TABLE IF NOT EXISTS farms (
                                     farm_id BIGINT NOT NULL AUTO_INCREMENT,
                                     farmer_id BIGINT NOT NULL,
                                     farm_name VARCHAR(100) NOT NULL,
    survey_number VARCHAR(50),
    address_line VARCHAR(200) NOT NULL,
    village VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    area_in_acres DECIMAL(10, 2) NOT NULL,
    soil_type VARCHAR(50) NOT NULL,
    ownership_type VARCHAR(30) NOT NULL,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_farms
    PRIMARY KEY (farm_id),

    CONSTRAINT fk_farms_farmer
    FOREIGN KEY (farmer_id)
    REFERENCES farmer_profiles (farmer_id),

    INDEX idx_farms_farmer_id (farmer_id),
    INDEX idx_farms_district (district),
    INDEX idx_farms_status (status)
    );

CREATE TABLE IF NOT EXISTS crops (
                                     crop_id BIGINT NOT NULL AUTO_INCREMENT,
                                     farm_id BIGINT NOT NULL,
                                     crop_type VARCHAR(50) NOT NULL,
    variety VARCHAR(100),
    sowing_date DATE NOT NULL,
    expected_harvest_date DATE NOT NULL,
    growth_stage VARCHAR(30) NOT NULL,
    irrigation_requirement VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_crops
    PRIMARY KEY (crop_id),

    CONSTRAINT fk_crops_farm
    FOREIGN KEY (farm_id)
    REFERENCES farms (farm_id),

    INDEX idx_crops_farm_id (farm_id),
    INDEX idx_crops_crop_type (crop_type),
    INDEX idx_crops_status (status)
    );

CREATE TABLE IF NOT EXISTS irrigation_profiles (
                                                   irrigation_id BIGINT NOT NULL AUTO_INCREMENT,
                                                   farm_id BIGINT NOT NULL,
                                                   water_source VARCHAR(30) NOT NULL,
    irrigation_method VARCHAR(30) NOT NULL,
    equipment_condition VARCHAR(20) NOT NULL,
    backup_water_source VARCHAR(30),
    historical_failure_count INT NOT NULL DEFAULT 0,
    average_water_availability_pct DECIMAL(5, 2) NOT NULL,
    seasonal_risk VARCHAR(20) NOT NULL,
    maintenance_frequency VARCHAR(20) NOT NULL,
    last_maintenance_date DATE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_irrigation_profiles
    PRIMARY KEY (irrigation_id),

    CONSTRAINT uk_irrigation_profiles_farm
    UNIQUE (farm_id),

    CONSTRAINT fk_irrigation_profiles_farm
    FOREIGN KEY (farm_id)
    REFERENCES farms (farm_id),

    INDEX idx_irrigation_water_source (water_source),
    INDEX idx_irrigation_seasonal_risk (seasonal_risk)
    );