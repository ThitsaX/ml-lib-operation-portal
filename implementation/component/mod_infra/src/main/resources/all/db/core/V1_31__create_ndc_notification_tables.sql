/*
 * NDC threshold configuration, runtime state, notification alert events,
 * delivery tracking, and job execution evaluation snapshots.
 */

ALTER TABLE tbl_job_execution_log
    ADD COLUMN participant_name VARCHAR(100) NULL AFTER execution_message,
    ADD COLUMN currency VARCHAR(100) NULL AFTER participant_name,
    ADD COLUMN ndc_used_percent DECIMAL(7,4) NULL AFTER currency,
    ADD COLUMN threshold_percent DECIMAL(7,4) NULL AFTER ndc_used_percent;

CREATE INDEX idx_job_execution_participant_currency
    ON tbl_job_execution_log (participant_name, currency);

CREATE INDEX idx_job_execution_ndc_history
    ON tbl_job_execution_log (job_name, start_time, participant_name, currency);

CREATE TABLE IF NOT EXISTS tbl_threshold_configuration (
    id BIGINT NOT NULL,
    scope_type VARCHAR(20) NOT NULL,
    participant_currency_id BIGINT NULL,
    threshold_enabled TINYINT(1) NOT NULL DEFAULT 0,
    color_code VARCHAR(20) NULL,
    visual_alert_percent DECIMAL(7,4) NOT NULL,
    noti_alert_percent DECIMAL(7,4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(100) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_threshold_config_scope
        UNIQUE (scope_type, participant_currency_id),
    CONSTRAINT chk_threshold_config_scope
        CHECK (
        scope_type = 'DFSP' AND participant_currency_id IS NOT NULL
        ),
    CONSTRAINT chk_threshold_config_scope_type
        CHECK (scope_type IN ('SCHEME', 'DFSP')),
    CONSTRAINT chk_threshold_config_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_threshold_config_scope
    ON tbl_threshold_configuration (scope_type);

CREATE INDEX idx_threshold_config_participant_currency
    ON tbl_threshold_configuration (participant_currency_id);

CREATE INDEX idx_threshold_config_gate
    ON tbl_threshold_configuration (scope_type, threshold_enabled, status);

CREATE TABLE IF NOT EXISTS tbl_ndc_threshold_state (
    id BIGINT NOT NULL,
    participant_ndc_id BIGINT NOT NULL,
    current_state VARCHAR(20) NOT NULL DEFAULT 'SAFE',
    breach_cycle_no BIGINT NOT NULL DEFAULT 0,
    last_evaluated_balance DECIMAL(18,4) NULL,
    last_evaluated_ndc_used DECIMAL(7,4) NULL,
    last_breached_at TIMESTAMP NULL,
    last_recovered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(100) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ndc_threshold_state_participant
        UNIQUE (participant_ndc_id),
    CONSTRAINT fk_ndc_state_participant_ndc
        FOREIGN KEY (participant_ndc_id)
        REFERENCES tbl_participant_ndc (participant_ndc_id),
    CONSTRAINT chk_ndc_threshold_state
        CHECK (current_state IN ('SAFE', 'BREACHED')),
    CONSTRAINT chk_ndc_breach_cycle
        CHECK (breach_cycle_no >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ndc_state_current_state
    ON tbl_ndc_threshold_state (current_state);

CREATE TABLE IF NOT EXISTS tbl_ndc_alert_event (
    id BIGINT NOT NULL,
    participant_ndc_id BIGINT NOT NULL,
    participant_name VARCHAR(100) NOT NULL,
    currency VARCHAR(100) NOT NULL,
    breach_cycle_no BIGINT NOT NULL,
    previous_state VARCHAR(20) NOT NULL,
    current_state VARCHAR(20) NOT NULL,
    threshold_percent DECIMAL(7,4) NOT NULL,
    current_balance DECIMAL(18,4) NOT NULL,
    current_ndc_used DECIMAL(7,4) NOT NULL,
    event_message TEXT NULL,
    event_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(100) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ndc_alert_breach_cycle
        UNIQUE (participant_ndc_id, breach_cycle_no),
    CONSTRAINT fk_ndc_alert_participant_ndc
        FOREIGN KEY (participant_ndc_id)
        REFERENCES tbl_participant_ndc (participant_ndc_id),
    CONSTRAINT chk_ndc_alert_previous_state
        CHECK (previous_state IN ('SAFE', 'BREACHED')),
    CONSTRAINT chk_ndc_alert_current_state
        CHECK (current_state IN ('SAFE', 'BREACHED')),
    CONSTRAINT chk_ndc_alert_transition
        CHECK (previous_state = 'SAFE' AND current_state = 'BREACHED'),
    CONSTRAINT chk_ndc_alert_breach_cycle
        CHECK (breach_cycle_no > 0),
    CONSTRAINT chk_ndc_alert_threshold_percent
        CHECK (threshold_percent >= 0 AND threshold_percent <= 100),
    CONSTRAINT chk_ndc_alert_used_percent
        CHECK (current_ndc_used >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ndc_alert_participant_ndc_time
    ON tbl_ndc_alert_event (participant_ndc_id, event_time);

CREATE TABLE IF NOT EXISTS tbl_ndc_notification_dispatch_log (
    id BIGINT NOT NULL,
    alert_event_id BIGINT NOT NULL,
    participant_ndc_id BIGINT NOT NULL,
    recipient_type VARCHAR(20) NOT NULL,
    recipient_user_id VARCHAR(100) NULL,
    recipient_name VARCHAR(100) NULL,
    recipient_email VARCHAR(150) NULL,
    delivery_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_no INT NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP NULL,
    sent_at TIMESTAMP NULL,
    error_message TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(100) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ndc_dispatch_alert_recipient
        UNIQUE (alert_event_id, recipient_user_id),
    CONSTRAINT fk_ndc_dispatch_alert
        FOREIGN KEY (alert_event_id)
        REFERENCES tbl_ndc_alert_event (id),
    CONSTRAINT fk_ndc_dispatch_participant_ndc
        FOREIGN KEY (participant_ndc_id)
        REFERENCES tbl_participant_ndc (participant_ndc_id),
    CONSTRAINT chk_ndc_dispatch_recipient_type
        CHECK (recipient_type IN ('HUB', 'DFSP')),
    CONSTRAINT chk_ndc_dispatch_status
        CHECK (delivery_status IN ('PENDING', 'SENT', 'FAILED', 'RETRYING')),
    CONSTRAINT chk_ndc_dispatch_attempt
        CHECK (attempt_no >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ndc_dispatch_retry
    ON tbl_ndc_notification_dispatch_log (delivery_status, last_attempt_at, attempt_no);

CREATE INDEX idx_ndc_dispatch_alert_status
    ON tbl_ndc_notification_dispatch_log (alert_event_id, delivery_status);

CREATE INDEX idx_ndc_dispatch_participant_status
    ON tbl_ndc_notification_dispatch_log (participant_ndc_id, delivery_status);
