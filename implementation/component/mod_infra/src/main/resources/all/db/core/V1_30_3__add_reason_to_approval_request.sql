ALTER TABLE `tbl_approval_request`
    ADD COLUMN `reason` VARCHAR(255) DEFAULT NULL AFTER `responded_dtm`;
