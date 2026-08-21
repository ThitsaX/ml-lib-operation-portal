INSERT INTO tbl_menu (menu_id, name, parent_id, is_active, created_date, updated_date)
SELECT 32, 'Role Permissions', '28', 1, 1787286230, 1787286230
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_menu WHERE name = 'Role Permissions'
);
