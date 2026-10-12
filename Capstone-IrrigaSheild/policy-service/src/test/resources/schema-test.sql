CREATE TABLE IF NOT EXISTS policies (
                                        policy_id BIGINT NOT NULL AUTO_INCREMENT,
                                        application_id BIGINT NOT NULL,
                                        farmer_user_id BIGINT NOT NULL,
                                        farmer_email VARCHAR(150) NOT NULL,
    farm_id BIGINT NOT NULL,
    crop_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,

    coverage_amount DECIMAL(12, 2) NOT NULL,
    premium_amount DECIMAL(12, 2) NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    payment_status VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,

    accepted_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (policy_id),
    UNIQUE KEY uk_policies_application (application_id),

    INDEX idx_policies_farmer (farmer_user_id),
    INDEX idx_policies_status (status)
    );