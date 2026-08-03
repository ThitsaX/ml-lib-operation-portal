INSERT INTO tbl_menu (menu_id, name, parent_id, is_active, created_date, updated_date)
SELECT 1000, 'Revenue Sharing Settings', '1', 1, 1785741412, 1785741412
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_menu WHERE name = 'Revenue Sharing Settings'
);

INSERT INTO tbl_menu (menu_id, name, parent_id, is_active, created_date, updated_date)
SELECT 1001, 'Party Registry', '1000', 1, 1785741412, 1785741412
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_menu WHERE name = 'Party Registry'
);

INSERT INTO tbl_menu (menu_id, name, parent_id, is_active, created_date, updated_date)
SELECT 1002, 'Revenue Config', '1000', 1, 1785741412, 1785741412
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_menu WHERE name = 'Revenue Config'
);

INSERT INTO tbl_menu (menu_id, name, parent_id, is_active, created_date, updated_date)
SELECT 1003, 'Revenue Rounding Setting', '1000', 1, 1785741412, 1785741412
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_menu WHERE name = 'Revenue Rounding Setting'
);

INSERT INTO tbl_menu (menu_id, name, parent_id, is_active, created_date, updated_date)
SELECT 1004, 'Revenue Sharing Summary Report', '13', 1, 1785741412, 1785741412
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_menu WHERE name = 'Revenue Sharing Summary Report'
);

INSERT INTO tbl_menu (menu_id, name, parent_id, is_active, created_date, updated_date)
SELECT 1005, 'Revenue Sharing Detailed Report', '13', 1, 1785741412, 1785741412
    WHERE NOT EXISTS (
    SELECT 1 FROM tbl_menu WHERE name = 'Revenue Sharing Detailed Report'
);







