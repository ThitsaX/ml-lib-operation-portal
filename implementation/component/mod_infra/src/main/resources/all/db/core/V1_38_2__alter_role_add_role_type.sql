ALTER TABLE `tbl_role`
    ADD COLUMN `role_type` VARCHAR(32) NULL AFTER `is_dfsp`;

UPDATE `tbl_role`
SET `role_type` = CASE
    WHEN UPPER(`name`) LIKE 'HUB%' THEN 'HUB'
    WHEN UPPER(`name`) LIKE 'DFSP%' THEN 'DFSP'
    WHEN UPPER(`name`) LIKE 'SYSTEM%' THEN 'SYSTEM'
    WHEN UPPER(`name`) LIKE 'REVENUE%' THEN 'LRA'
    ELSE `role_type`
END;
