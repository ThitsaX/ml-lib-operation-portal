ALTER TABLE tbl_user ADD COLUMN is_visible BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE tbl_user SET is_visible = FALSE WHERE LOWER(email) = 'systemadmin@thitsaworks.com';
