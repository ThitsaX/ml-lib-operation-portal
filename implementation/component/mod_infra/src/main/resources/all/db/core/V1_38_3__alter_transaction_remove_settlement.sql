ALTER TABLE tbl_transaction
    DROP COLUMN settlement_id,
    MODIFY COLUMN hub_transaction_id VARCHAR(100) NULL;
