/*
 * Align NDC scheduler timezone and store NDC business timestamps as Unix epoch seconds.
 */

UPDATE tbl_scheduler_config
SET zone_id = '+00:00',
    updated_date = UNIX_TIMESTAMP()
WHERE name IN ('NdcThresholdWorkerSync', 'NdcNotificationDispatcherSync');

CREATE TEMPORARY TABLE tmp_ndc_threshold_state_event_timestamps AS
SELECT id,
       UNIX_TIMESTAMP(last_breached_at)  AS last_breached_at,
       UNIX_TIMESTAMP(last_recovered_at) AS last_recovered_at
FROM tbl_ndc_threshold_state;

ALTER TABLE tbl_ndc_threshold_state
    MODIFY COLUMN last_breached_at BIGINT DEFAULT NULL,
    MODIFY COLUMN last_recovered_at BIGINT DEFAULT NULL;

UPDATE tbl_ndc_threshold_state state
    JOIN tmp_ndc_threshold_state_event_timestamps converted
    ON converted.id = state.id
SET state.last_breached_at = converted.last_breached_at,
    state.last_recovered_at = converted.last_recovered_at;

DROP TEMPORARY TABLE tmp_ndc_threshold_state_event_timestamps;

CREATE TEMPORARY TABLE tmp_ndc_alert_event_timestamps AS
SELECT id,
       UNIX_TIMESTAMP(event_time) AS event_time
FROM tbl_ndc_alert_event;

ALTER TABLE tbl_ndc_alert_event
    DROP INDEX idx_ndc_alert_participant_ndc_time,
    MODIFY COLUMN event_time BIGINT NOT NULL;

UPDATE tbl_ndc_alert_event event
    JOIN tmp_ndc_alert_event_timestamps converted
    ON converted.id = event.id
SET event.event_time = converted.event_time;

ALTER TABLE tbl_ndc_alert_event
    ADD INDEX idx_ndc_alert_participant_ndc_time (participant_ndc_id, event_time);

DROP TEMPORARY TABLE tmp_ndc_alert_event_timestamps;

CREATE TEMPORARY TABLE tmp_ndc_notification_dispatch_log_timestamps AS
SELECT id,
       UNIX_TIMESTAMP(last_attempt_at) AS last_attempt_at,
       UNIX_TIMESTAMP(sent_at)         AS sent_at
FROM tbl_ndc_notification_dispatch_log;

ALTER TABLE tbl_ndc_notification_dispatch_log
    DROP INDEX idx_ndc_dispatch_retry,
    MODIFY COLUMN last_attempt_at BIGINT DEFAULT NULL,
    MODIFY COLUMN sent_at BIGINT DEFAULT NULL;

UPDATE tbl_ndc_notification_dispatch_log log
    JOIN tmp_ndc_notification_dispatch_log_timestamps converted
    ON converted.id = log.id
SET log.last_attempt_at = converted.last_attempt_at,
    log.sent_at = converted.sent_at;

ALTER TABLE tbl_ndc_notification_dispatch_log
    ADD INDEX idx_ndc_dispatch_retry (delivery_status, last_attempt_at, attempt_no);

DROP TEMPORARY TABLE tmp_ndc_notification_dispatch_log_timestamps;
