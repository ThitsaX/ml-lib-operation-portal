ALTER TABLE `tbl_approval_request`
    ADD COLUMN `request_category` VARCHAR(50) DEFAULT NULL AFTER `updated_date`,
    ADD COLUMN `submitted_at` BIGINT DEFAULT NULL AFTER `request_category`,
    ADD COLUMN `decided_at` BIGINT DEFAULT NULL AFTER `submitted_at`;

ALTER TABLE `tbl_approval_request`
    MODIFY COLUMN `participant_name` VARCHAR(100) DEFAULT NULL,
    MODIFY COLUMN `participant_currency` VARCHAR(32) DEFAULT NULL,
    MODIFY COLUMN `participant_settlement_currency_id` VARCHAR(100) DEFAULT NULL,
    MODIFY COLUMN `participant_position_currency_id` VARCHAR(100) DEFAULT NULL;

CREATE TABLE IF NOT EXISTS `tbl_approval_request_field_detail` (
    `approval_request_field_detail_id` BIGINT NOT NULL,
    `approval_request_id`              BIGINT NOT NULL,
    `field_key`                        VARCHAR(100) NOT NULL,
    `field_label`                      VARCHAR(150) NOT NULL,
    `field_value`                      MEDIUMTEXT NULL,
    `before_value`                     MEDIUMTEXT NULL,
    `after_value`                      MEDIUMTEXT NULL,
    `value_type`                       VARCHAR(20) NOT NULL,
    `display_order`                    INT NOT NULL,
    `tab_code`                         VARCHAR(50) DEFAULT NULL,
    `created_date`                     BIGINT DEFAULT NULL,
    `updated_date`                     BIGINT DEFAULT NULL,
    PRIMARY KEY (`approval_request_field_detail_id`),
    KEY `idx_tbl_approval_request_field_detail_request_id` (`approval_request_id`),
    KEY `idx_tbl_approval_request_field_detail_tab_code` (`tab_code`),
    KEY `idx_tbl_approval_request_field_detail_field_key` (`field_key`),
    CONSTRAINT `fk_tbl_approval_request_field_detail_request_id`
        FOREIGN KEY (`approval_request_id`) REFERENCES `tbl_approval_request` (`approval_request_id`)
);
