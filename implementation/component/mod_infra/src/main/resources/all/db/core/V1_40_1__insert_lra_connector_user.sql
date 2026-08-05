INSERT INTO tbl_user (user_id, participant_id, name, email, first_name, last_name, job_title, created_date, updated_date, is_deleted, is_visible)
SELECT 1111111111111114, 1111111111111111, 'LRA Connector', 'lra.connector@thitsaworks.com', 'LRA', 'Connector', 'LRA Connector', 1786011730, 1786011730, 0, 0
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_user WHERE LOWER(email) = 'lra.connector@thitsaworks.com'
);

INSERT INTO tbl_principal (principal_id, access_key, secret_key, realm_id, sha_256_password_hex, status, created_date, updated_date)
SELECT 1111111111111114, 411194012689530883, 'ea3184c0-0c70-4ab5-af24-adb3ac3b6885', 1111111111111111, '179588AA09CFF63F9230F9D80FB2D294159E3AABABD22B3338E922A879F92ECA', 'ACTIVE', 0, NULL
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_principal WHERE principal_id = 1111111111111114
);

INSERT INTO tbl_principal_role (principal_role_id, role_id, principal_id, created_date, updated_date)
SELECT 4, 8, 1111111111111114, 1786011730, 1786011730
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_principal_role WHERE principal_role_id = 4
);
