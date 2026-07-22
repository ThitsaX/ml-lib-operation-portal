/*
 * Per-DFSP and per-currency visual and NDC notification thresholds.
 */

CREATE TABLE IF NOT EXISTS tbl_threshold_detail (
    id BIGINT NOT NULL,
    threshold_id BIGINT NOT NULL,
    participant_currency_id BIGINT NOT NULL,
    currency VARCHAR(100) NOT NULL,
    visual_config DECIMAL(7,4) NOT NULL,
    ndc_config DECIMAL(7,4) NOT NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NULL,
    updated_by VARCHAR(100) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_threshold_detail_configuration
        FOREIGN KEY (threshold_id)
        REFERENCES tbl_threshold_configuration (id),
    CONSTRAINT uk_threshold_detail_currency
        UNIQUE (threshold_id, currency),
    CONSTRAINT uk_threshold_detail_participant_currency
        UNIQUE (participant_currency_id),
    CONSTRAINT chk_threshold_detail_visual
        CHECK (visual_config >= 0 AND visual_config <= 100),
    CONSTRAINT chk_threshold_detail_ndc
        CHECK (ndc_config >= 0 AND ndc_config <= 100),
    CONSTRAINT chk_threshold_detail_order
        CHECK (visual_config <= ndc_config)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_threshold_detail_threshold_status
    ON tbl_threshold_detail (threshold_id, status);

CREATE INDEX idx_threshold_detail_currency_status
    ON tbl_threshold_detail (currency, status);

/* The simplified worker no longer fetches a current position separately. */
ALTER TABLE tbl_ndc_alert_event
    MODIFY COLUMN current_balance DECIMAL(18,4) NULL;
