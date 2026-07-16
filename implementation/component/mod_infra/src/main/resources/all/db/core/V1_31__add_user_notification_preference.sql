ALTER TABLE tbl_user
    ADD COLUMN allow_notification TINYINT(1) NOT NULL DEFAULT 0 AFTER job_title;
