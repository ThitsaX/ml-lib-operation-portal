INSERT INTO tbl_menu (menu_id, name, parent_id, is_active, created_date, updated_date)
SELECT 33, 'NDC Alert Settings', '28', 1, 1785400501, 1785400501
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_menu WHERE name = 'NDC Alert Settings'
);

INSERT INTO tbl_menu (menu_id, name, parent_id, is_active, created_date, updated_date)
SELECT 34, 'Notification Delivery Log', '1', 1, 1785735625, 1785735625
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_menu WHERE name = 'Notification Delivery Log'
);

ALTER TABLE tbl_ndc_threshold_state
    MODIFY COLUMN last_evaluated_ndc_used DECIMAL(18,4) NULL;

ALTER TABLE tbl_ndc_alert_event
    MODIFY COLUMN current_ndc_used DECIMAL(18,4) NOT NULL;

ALTER TABLE tbl_job_execution_log
    MODIFY COLUMN ndc_used_percent DECIMAL(18,4) NULL;

ALTER TABLE tbl_job_execution_log
    MODIFY COLUMN threshold_percent DECIMAL(18,4) NULL;

ALTER TABLE tbl_ndc_alert_event
    MODIFY COLUMN threshold_percent DECIMAL(18,4) NOT NULL;

ALTER TABLE tbl_threshold_detail
    MODIFY COLUMN visual_config DECIMAL(18,4) NOT NULL;

ALTER TABLE tbl_threshold_detail
    MODIFY COLUMN ndc_config DECIMAL(18,4) NOT NULL;
