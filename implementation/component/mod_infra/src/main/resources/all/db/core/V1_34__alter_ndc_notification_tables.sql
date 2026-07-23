/*
 * Move NDC runtime tables from the legacy participant NDC relationship to
 * participant/currency keys and retain the calculation inputs on alert events.
 */

ALTER TABLE tbl_ndc_threshold_state
    ADD COLUMN participant_name VARCHAR(100) NULL AFTER id,
    ADD COLUMN currency VARCHAR(100) NULL AFTER participant_name;

UPDATE tbl_ndc_threshold_state state
JOIN tbl_participant_ndc participant_ndc
    ON participant_ndc.participant_ndc_id = state.participant_ndc_id
SET state.participant_name = participant_ndc.dfsp_code,
    state.currency = participant_ndc.currency;

ALTER TABLE tbl_ndc_threshold_state
    MODIFY COLUMN participant_name VARCHAR(100) NOT NULL,
    MODIFY COLUMN currency VARCHAR(100) NOT NULL;

ALTER TABLE tbl_ndc_threshold_state
    DROP FOREIGN KEY fk_ndc_state_participant_ndc,
    DROP INDEX uk_ndc_threshold_state_participant,
    DROP COLUMN participant_ndc_id,
    ADD CONSTRAINT uk_ndc_threshold_state_participant_currency
        UNIQUE (participant_name, currency);

ALTER TABLE tbl_ndc_alert_event
    ADD COLUMN current_position DECIMAL(18,4) NULL AFTER threshold_percent,
    ADD COLUMN ndc_limit DECIMAL(18,4) NULL AFTER current_position;

UPDATE tbl_ndc_alert_event
SET current_position = current_balance,
    ndc_limit = CASE
        WHEN current_ndc_used > 0
            THEN ROUND(ABS(current_balance) * 100 / current_ndc_used, 4)
        ELSE 0
    END;

ALTER TABLE tbl_ndc_alert_event
    MODIFY COLUMN current_position DECIMAL(18,4) NOT NULL,
    MODIFY COLUMN ndc_limit DECIMAL(18,4) NOT NULL;

ALTER TABLE tbl_ndc_alert_event
    DROP FOREIGN KEY fk_ndc_alert_participant_ndc,
    DROP INDEX uk_ndc_alert_breach_cycle,
    DROP INDEX idx_ndc_alert_participant_ndc_time,
    DROP COLUMN participant_ndc_id,
    DROP COLUMN current_balance,
    ADD CONSTRAINT uk_ndc_alert_breach_cycle
        UNIQUE (participant_name, currency, breach_cycle_no);

CREATE INDEX idx_ndc_alert_participant_currency_time
    ON tbl_ndc_alert_event (participant_name, currency, event_time);

ALTER TABLE tbl_ndc_notification_dispatch_log
    DROP FOREIGN KEY fk_ndc_dispatch_participant_ndc,
    DROP INDEX idx_ndc_dispatch_participant_status,
    DROP COLUMN participant_ndc_id;
