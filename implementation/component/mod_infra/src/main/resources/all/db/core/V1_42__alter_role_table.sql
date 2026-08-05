SET @add_role_type_sql = IF(
    EXISTS (
        SELECT 1
        FROM `information_schema`.`COLUMNS`
        WHERE `TABLE_SCHEMA` = DATABASE()
          AND `TABLE_NAME` = 'tbl_role'
          AND `COLUMN_NAME` = 'role_type'
    ),
    'SELECT 1',
    'ALTER TABLE `tbl_role` ADD COLUMN `role_type` VARCHAR(32) NULL AFTER `active`'
);

PREPARE add_role_type_stmt FROM @add_role_type_sql;
EXECUTE add_role_type_stmt;
DEALLOCATE PREPARE add_role_type_stmt;

UPDATE `tbl_role`
SET `role_type` = CASE
    WHEN UPPER(`name`) LIKE 'HUB%' THEN 'HUB'
    WHEN UPPER(`name`) LIKE 'DFSP%' THEN 'DFSP'
    WHEN UPPER(`name`) LIKE 'SYSTEM%' THEN 'SYSTEM'
    WHEN UPPER(`name`) LIKE 'REVENUE%' THEN 'LRA'
END
WHERE `role_type` IS NULL
  AND (
      UPPER(`name`) LIKE 'HUB%'
      OR UPPER(`name`) LIKE 'DFSP%'
      OR UPPER(`name`) LIKE 'SYSTEM%'
      OR UPPER(`name`) LIKE 'REVENUE%'
  );

SET @drop_is_dfsp_sql = IF(
    EXISTS (
        SELECT 1
        FROM `information_schema`.`COLUMNS`
        WHERE `TABLE_SCHEMA` = DATABASE()
          AND `TABLE_NAME` = 'tbl_role'
          AND `COLUMN_NAME` = 'is_dfsp'
    ),
    'ALTER TABLE `tbl_role` DROP COLUMN `is_dfsp`',
    'SELECT 1'
);

PREPARE drop_is_dfsp_stmt FROM @drop_is_dfsp_sql;
EXECUTE drop_is_dfsp_stmt;
DEALLOCATE PREPARE drop_is_dfsp_stmt;
