ALTER TABLE tbl_user
    ADD COLUMN allow_notification TINYINT(1) NOT NULL DEFAULT 0 AFTER job_title;

INSERT INTO tbl_scheduler_config
    (scheduler_config_id, name, job_name, description, cron_expression, zone_id, is_active, created_date, updated_date)
VALUES
    (1111111111111113,
     'NdcThresholdWorkerSync',
     'NdcThresholdWorker',
     'Executes every 5 minutes to evaluate NDC threshold alerts.',
     '0 */5 * * * ?',
     '+00:00',
     1,
     UNIX_TIMESTAMP(),
     UNIX_TIMESTAMP())
ON DUPLICATE KEY UPDATE
    job_name        = VALUES(job_name),
    description     = VALUES(description),
    cron_expression = VALUES(cron_expression),
    zone_id         = VALUES(zone_id),
    is_active       = VALUES(is_active),
    updated_date    = VALUES(updated_date);

INSERT INTO tbl_scheduler_config
    (scheduler_config_id, name, job_name, description, cron_expression, zone_id, is_active, created_date, updated_date)
VALUES
    (1111111111111114,
     'NdcNotificationDispatcherSync',
     'NdcNotificationDispatcher',
     'Executes every 5 seconds to dispatch pending NDC notifications.',
     '*/5 * * * * *',
     '+00:00',
     1,
     UNIX_TIMESTAMP(),
     UNIX_TIMESTAMP())
ON DUPLICATE KEY UPDATE
    job_name        = VALUES(job_name),
    description     = VALUES(description),
    cron_expression = VALUES(cron_expression),
    zone_id         = VALUES(zone_id),
    is_active       = VALUES(is_active),
    updated_date    = VALUES(updated_date);
