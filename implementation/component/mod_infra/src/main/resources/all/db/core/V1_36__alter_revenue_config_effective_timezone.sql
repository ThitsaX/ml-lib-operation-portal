ALTER TABLE `tbl_revenue_config`
    ADD COLUMN `effective_timezone` VARCHAR(50) DEFAULT NULL AFTER `effective_date`;

ALTER TABLE `tbl_revenue_config_history`
    ADD COLUMN `effective_timezone` VARCHAR(50) DEFAULT NULL AFTER `effective_date`;
