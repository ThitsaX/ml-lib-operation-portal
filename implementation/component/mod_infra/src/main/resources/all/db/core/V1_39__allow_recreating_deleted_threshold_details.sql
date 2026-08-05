/*
 * Retain deleted threshold details as history while allowing a new active
 * record for the same DFSP configuration and currency.
 */

UPDATE tbl_approval_request
SET requested_action = 'DELETE_NDC_ALERT_THRESHOLD'
WHERE request_category = 'NDC_ALERT_THRESHOLD'
  AND requested_action = 'DISABLE_NDC_ALERT';

ALTER TABLE tbl_threshold_detail
    DROP INDEX uk_threshold_detail_currency;

ALTER TABLE tbl_threshold_detail
    ADD COLUMN active_currency VARCHAR(100)
        GENERATED ALWAYS AS (
            CASE
                WHEN status = 1 THEN currency
                ELSE NULL
            END
        ) STORED;

ALTER TABLE tbl_threshold_detail
    ADD CONSTRAINT uk_threshold_detail_active_currency
        UNIQUE (threshold_id, active_currency);
