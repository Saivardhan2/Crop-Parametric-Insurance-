CREATE TABLE IF NOT EXISTS farms (
                                     farm_id BIGINT NOT NULL AUTO_INCREMENT,
                                     farmer_id BIGINT NOT NULL,

                                     farm_name VARCHAR(100) NOT NULL,
    survey_number VARCHAR(100),

    address_line VARCHAR(200) NOT NULL,
    village VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,

    area_in_acres DECIMAL(10, 2) NOT NULL,
    soil_type VARCHAR(50) NOT NULL,
    ownership_type VARCHAR(50) NOT NULL,

    latitude DECIMAL(10, 6),
    longitude DECIMAL(10, 6),

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (farm_id),

    UNIQUE KEY uk_farmer_farm_name (
                                       farmer_id,
                                       farm_name
                                   ),

    INDEX idx_farms_farmer_id (
                                  farmer_id
                              ),

    INDEX idx_farms_status (
                               status
                           )
    );