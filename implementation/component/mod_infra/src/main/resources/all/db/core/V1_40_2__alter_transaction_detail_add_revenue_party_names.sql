ALTER TABLE tbl_transaction_detail
    ADD COLUMN responsible_ministry_name VARCHAR(255) NULL AFTER responsible_ministry_code,
    ADD COLUMN third_party_name VARCHAR(255) NULL AFTER third_party_code;
