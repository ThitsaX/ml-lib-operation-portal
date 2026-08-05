ALTER TABLE tbl_user
ADD COLUMN is_visible TINYINT(1) NOT NULL DEFAULT 1;

UPDATE tbl_user
SET is_visible = 0
WHERE LOWER(email) = 'systemadmin@thitsaworks.com';