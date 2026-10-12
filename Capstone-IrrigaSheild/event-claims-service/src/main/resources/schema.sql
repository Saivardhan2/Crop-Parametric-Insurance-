CREATE TABLE IF NOT EXISTS irrigation_readings (
    reading_id BIGINT NOT NULL AUTO_INCREMENT,
    policy_id BIGINT NOT NULL,
    farm_id BIGINT NOT NULL,
    availability_percentage DECIMAL(5, 2) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    submitted_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_irrigation_readings
    PRIMARY KEY (reading_id),

    INDEX idx_readings_policy (policy_id),
    INDEX idx_readings_farm (farm_id)
);

CREATE TABLE IF NOT EXISTS claims (
    claim_id BIGINT NOT NULL AUTO_INCREMENT,
    reading_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL,
    event_date DATE NOT NULL,
    farmer_user_id BIGINT NOT NULL,
    farm_id BIGINT NOT NULL,

    availability_percentage DECIMAL(5, 2) NOT NULL,
    payout_percentage DECIMAL(5, 2) NOT NULL,
    coverage_amount DECIMAL(12, 2) NOT NULL,
    compensation_amount DECIMAL(12, 2) NOT NULL,

    payout_status VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP NULL,

    CONSTRAINT pk_claims
    PRIMARY KEY (claim_id),

    CONSTRAINT fk_claims_reading
    FOREIGN KEY (reading_id)
    REFERENCES irrigation_readings (reading_id),

    CONSTRAINT uk_claims_reading
    UNIQUE (reading_id),

    INDEX idx_claims_policy (policy_id),
    INDEX idx_claims_policy_event (policy_id, event_date),
    INDEX idx_claims_farmer (farmer_user_id),
    INDEX idx_claims_status (status)
);
