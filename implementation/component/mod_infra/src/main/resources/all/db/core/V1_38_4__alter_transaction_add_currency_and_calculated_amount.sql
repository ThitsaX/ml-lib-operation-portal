ALTER TABLE tbl_transaction
    MODIFY COLUMN bill_number VARCHAR(100) NULL,
    MODIFY COLUMN bill_date VARCHAR(100) NULL,
    ADD COLUMN sent_currency VARCHAR(10) NULL AFTER amount_currency;

ALTER TABLE tbl_transaction_detail
    ADD COLUMN calculated_amount DECIMAL(19,4) NULL AFTER tax_amount_ch;
