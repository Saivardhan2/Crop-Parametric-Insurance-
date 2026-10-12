CREATE TABLE IF NOT EXISTS insurance_plans (
                                               plan_id BIGINT NOT NULL AUTO_INCREMENT,
                                               plan_name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    minimum_coverage DECIMAL(12, 2) NOT NULL,
    maximum_coverage DECIMAL(12, 2) NOT NULL,
    base_premium DECIMAL(12, 2) NOT NULL,
    maximum_policy_days INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_insurance_plans
    PRIMARY KEY (plan_id),

    CONSTRAINT uk_insurance_plans_name
    UNIQUE (plan_name),

    INDEX idx_insurance_plans_status (status)
    );

CREATE TABLE IF NOT EXISTS trigger_rules (
                                             trigger_rule_id BIGINT NOT NULL AUTO_INCREMENT,
                                             plan_id BIGINT NOT NULL,
                                             crop_type VARCHAR(50) NOT NULL,
    growth_stage VARCHAR(30) NOT NULL,
    availability_threshold_pct DECIMAL(5, 2) NOT NULL,
    minimum_continuous_hours INT NOT NULL,
    rolling_window_days INT NOT NULL,
    required_shortage_days INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_trigger_rules
    PRIMARY KEY (trigger_rule_id),

    CONSTRAINT fk_trigger_rules_plan
    FOREIGN KEY (plan_id)
    REFERENCES insurance_plans (plan_id),

    CONSTRAINT uk_trigger_rule_plan_crop_stage
    UNIQUE (plan_id, crop_type, growth_stage),

    INDEX idx_trigger_rules_plan_id (plan_id),
    INDEX idx_trigger_rules_crop_type (crop_type),
    INDEX idx_trigger_rules_status (status)
    );

CREATE TABLE IF NOT EXISTS payout_slabs (
    payout_slab_id BIGINT NOT NULL AUTO_INCREMENT,
    trigger_rule_id BIGINT NOT NULL,
    minimum_duration_hours INT DEFAULT 0,
    maximum_duration_hours INT,
    min_availability_pct DECIMAL(5, 2),
    max_availability_pct DECIMAL(5, 2),
    payout_percentage DECIMAL(5, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payout_slabs
    PRIMARY KEY (payout_slab_id),

    CONSTRAINT fk_payout_slabs_trigger_rule
    FOREIGN KEY (trigger_rule_id)
    REFERENCES trigger_rules (trigger_rule_id),

    INDEX idx_payout_slabs_trigger_rule (trigger_rule_id),
    INDEX idx_payout_slabs_status (status)
);

CREATE TABLE IF NOT EXISTS policy_applications (
                                                   application_id BIGINT NOT NULL AUTO_INCREMENT,
                                                   farmer_user_id BIGINT NOT NULL,
                                                   farmer_email VARCHAR(150) NOT NULL,
    farm_id BIGINT NOT NULL,
    crop_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    requested_coverage DECIMAL(12, 2) NOT NULL,
    proposed_start_date DATE NOT NULL,
    proposed_end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    submitted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_policy_applications
    PRIMARY KEY (application_id),

    CONSTRAINT fk_policy_applications_plan
    FOREIGN KEY (plan_id)
    REFERENCES insurance_plans (plan_id),

    INDEX idx_applications_farmer
(farmer_user_id),

    INDEX idx_applications_farm
(farm_id),

    INDEX idx_applications_status
(status)
    );

CREATE TABLE IF NOT EXISTS underwriting_cases (
  case_id BIGINT NOT NULL AUTO_INCREMENT,
  application_id BIGINT NOT NULL,

  underwriter_user_id BIGINT NOT NULL,
  underwriter_email VARCHAR(150) NOT NULL,

    water_availability_score INT,
    equipment_condition_score INT,
    historical_failure_score INT,
    seasonal_risk_score INT,
    backup_source_score INT,

    overall_risk_score INT,
    risk_level VARCHAR(20),

    base_premium DECIMAL(12, 2),
    risk_loading DECIMAL(12, 2),
    recommended_premium DECIMAL(12, 2),
    recommended_coverage DECIMAL(12, 2),

    decision VARCHAR(20),
    remarks VARCHAR(500),
    status VARCHAR(30) NOT NULL,

    review_started_at TIMESTAMP NULL,
    review_completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_underwriting_cases
    PRIMARY KEY (case_id),

    CONSTRAINT uk_underwriting_application
    UNIQUE (application_id),

    CONSTRAINT fk_underwriting_application
    FOREIGN KEY (application_id)
    REFERENCES policy_applications (application_id),

    INDEX idx_underwriting_status (status),
    INDEX idx_underwriting_underwriter (underwriter_user_id)
    );


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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_policies
    PRIMARY KEY (policy_id),

    CONSTRAINT uk_policies_application
    UNIQUE (application_id),

    CONSTRAINT fk_policies_application
    FOREIGN KEY (application_id)
    REFERENCES policy_applications (application_id),

    CONSTRAINT fk_policies_plan
    FOREIGN KEY (plan_id)
    REFERENCES insurance_plans (plan_id),

    INDEX idx_policies_farmer (farmer_user_id),
    INDEX idx_policies_status (status)
    );