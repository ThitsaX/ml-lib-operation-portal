CREATE TABLE IF NOT EXISTS `tbl_revenue_config` (
  `id` BIGINT NOT NULL,
  `tax_code_id` VARCHAR(50) NOT NULL,
  `tax_code_description` VARCHAR(255) NOT NULL,
  `category` VARCHAR(20) NOT NULL,
  `responsible_ministry_code` VARCHAR(50) NOT NULL,
  `third_party_provider_code` VARCHAR(50) DEFAULT NULL,
  `gol_percentage` DECIMAL(5,2) NOT NULL,
  `ministry_percentage` DECIMAL(5,2) NOT NULL,
  `third_party_percentage` DECIMAL(5,2) NOT NULL,
  `sending_dfsp_percentage` DECIMAL(5,2) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `effective_date` BIGINT DEFAULT NULL,
  `responded_date` BIGINT DEFAULT NULL,
  `created_date` BIGINT DEFAULT NULL,
  `updated_date` BIGINT DEFAULT NULL,
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tbl_revenue_config_tax_code_id` (`tax_code_id`),
  KEY `idx_tbl_revenue_config_status` (`status`),
  KEY `idx_tbl_revenue_config_responsible_ministry_code` (`responsible_ministry_code`),
  KEY `idx_tbl_revenue_config_third_party_provider_code` (`third_party_provider_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tbl_revenue_config_history` (
  `history_id` BIGINT NOT NULL,
  `revenue_config_id` BIGINT NOT NULL,
  `tax_code_id` VARCHAR(50) NOT NULL,
  `tax_code_description` VARCHAR(255) NOT NULL,
  `category` VARCHAR(20) NOT NULL,
  `responsible_ministry_code` VARCHAR(50) NOT NULL,
  `third_party_provider_code` VARCHAR(50) DEFAULT NULL,
  `gol_percentage` DECIMAL(5,2) NOT NULL,
  `ministry_percentage` DECIMAL(5,2) NOT NULL,
  `third_party_percentage` DECIMAL(5,2) NOT NULL,
  `sending_dfsp_percentage` DECIMAL(5,2) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `effective_date` BIGINT DEFAULT NULL,
  `responded_date` BIGINT DEFAULT NULL,
  `created_date` BIGINT DEFAULT NULL,
  `updated_date` BIGINT DEFAULT NULL,
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`history_id`),
  KEY `idx_tbl_revenue_config_history_config_id` (`revenue_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tbl_revenue_rounding_policy` (
  `id` BIGINT NOT NULL,
  `rounding_mode` VARCHAR(20) NOT NULL,
  `remainder_recipient` VARCHAR(30) NOT NULL,
  `created_date` BIGINT DEFAULT NULL,
  `updated_date` BIGINT DEFAULT NULL,
  `created_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tbl_revenue_rounding_policy_created_date` (`created_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
