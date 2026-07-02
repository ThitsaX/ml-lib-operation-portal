INSERT INTO tbl_menu (menu_id, name, parent_id, is_active, created_date, updated_date)
SELECT 28, 'System Settings', '1', 1, 1782191842, 1782191842
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_menu WHERE name = 'System Settings'
);

INSERT INTO tbl_user (user_id,participant_id,name,email,first_name,last_name,job_title,created_date,updated_date,is_deleted)
SELECT 1111111111111113,1111111111111111,'System Admin','systemadmin@thitsaworks.com','System','Admin','System Admin',1782191842,1782191842,0
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_user WHERE user_id = 1111111111111113
);

INSERT INTO tbl_principal (principal_id,access_key,secret_key,realm_id,sha_256_password_hex,status,created_date,updated_date)
SELECT 1111111111111113,411194012689530882,'ea3184c0-0c70-4ab5-af24-adb3ac3b6885',1111111111111111,'C8892F69729A4471153DFB90DD596EC0A8283BA33F2B3444C11D903B71203890','ACTIVE',0,NULL
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_principal WHERE principal_id = 1111111111111113
);

INSERT INTO tbl_principal_role (principal_role_id,role_id,principal_id,created_date,updated_date)
SELECT 3,7,1111111111111113,1782191842,1782191842
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_principal_role WHERE principal_role_id = 3
);
