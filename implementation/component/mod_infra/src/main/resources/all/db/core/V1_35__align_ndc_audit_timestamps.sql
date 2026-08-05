/*
 * Align NDC feature audit timestamps with the shared JpaEntity convention.
 * Existing TIMESTAMP values are preserved as Unix epoch seconds.
 */

ALTER TABLE tbl_threshold_configuration
    ADD COLUMN created_date BIGINT DEFAULT NULL,
    ADD COLUMN updated_date BIGINT DEFAULT NULL;

UPDATE tbl_threshold_configuration
SET created_date = UNIX_TIMESTAMP(created_at),
    updated_date = UNIX_TIMESTAMP(updated_at);

ALTER TABLE tbl_threshold_configuration
    DROP COLUMN created_at,
    DROP COLUMN updated_at;

ALTER TABLE tbl_threshold_detail
    ADD COLUMN created_date BIGINT DEFAULT NULL,
    ADD COLUMN updated_date BIGINT DEFAULT NULL;

UPDATE tbl_threshold_detail
SET created_date = UNIX_TIMESTAMP(created_at),
    updated_date = UNIX_TIMESTAMP(updated_at);

ALTER TABLE tbl_threshold_detail
    DROP COLUMN created_at,
    DROP COLUMN updated_at;

ALTER TABLE tbl_ndc_threshold_state
    ADD COLUMN created_date BIGINT DEFAULT NULL,
    ADD COLUMN updated_date BIGINT DEFAULT NULL;

UPDATE tbl_ndc_threshold_state
SET created_date = UNIX_TIMESTAMP(created_at),
    updated_date = UNIX_TIMESTAMP(updated_at);

ALTER TABLE tbl_ndc_threshold_state
    DROP COLUMN created_at,
    DROP COLUMN updated_at;

ALTER TABLE tbl_ndc_alert_event
    ADD COLUMN created_date BIGINT DEFAULT NULL,
    ADD COLUMN updated_date BIGINT DEFAULT NULL;

UPDATE tbl_ndc_alert_event
SET created_date = UNIX_TIMESTAMP(created_at),
    updated_date = UNIX_TIMESTAMP(updated_at);

ALTER TABLE tbl_ndc_alert_event
    DROP COLUMN created_at,
    DROP COLUMN updated_at;

ALTER TABLE tbl_ndc_notification_dispatch_log
    ADD COLUMN created_date BIGINT DEFAULT NULL,
    ADD COLUMN updated_date BIGINT DEFAULT NULL;

UPDATE tbl_ndc_notification_dispatch_log
SET created_date = UNIX_TIMESTAMP(created_at),
    updated_date = UNIX_TIMESTAMP(updated_at);

ALTER TABLE tbl_ndc_notification_dispatch_log
    DROP COLUMN created_at,
    DROP COLUMN updated_at;
