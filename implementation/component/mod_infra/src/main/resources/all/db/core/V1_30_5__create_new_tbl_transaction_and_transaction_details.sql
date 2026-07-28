CREATE TABLE IF NOT EXISTS tbl_transaction (
    id                  BIGINT          NOT NULL,
    hub_transaction_id  VARCHAR(100)    NOT NULL,
    settlement_id       BIGINT          NULL,
    tin                 VARCHAR(100)    NULL,
    tax_payer_name      VARCHAR(255)    NULL,
    bill_number         VARCHAR(100)    NOT NULL,
    bill_date           BIGINT          NULL,
    total_amount        DECIMAL(19,4)   NOT NULL,
    amount_currency     VARCHAR(10)     NOT NULL,
    rate_exchange       DECIMAL(19,8)   NULL,
    receipt_number      VARCHAR(100)    NULL,
    sender_dfsp_id      VARCHAR(100)    NULL,
    state               VARCHAR(100)    NOT NULL,
    created_date        BIGINT          NULL,
    updated_date        BIGINT          NULL,

    CONSTRAINT pk_tbl_transaction
        PRIMARY KEY (id),

    CONSTRAINT uk_transaction_hub_transaction_id
        UNIQUE (hub_transaction_id),

    CONSTRAINT chk_transaction_state
        CHECK (
            state IN (
                'WAITING_FOR_ACTION',
                'WAITING_FOR_PARTY_ACCEPTANCE',
                'QUOTE_REQUEST_RECEIVED',
                'WAITING_FOR_QUOTE_ACCEPTANCE',
                'PREPARE_RECEIVED',
                'ERROR_OCCURRED',
                'COMPLETED',
                'ABORTED',
                'RESERVED'
            )
        ),

    INDEX idx_transaction_settlement_id (settlement_id),
    INDEX idx_transaction_bill_number (bill_number)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS tbl_transaction_detail (
    id                              BIGINT          NOT NULL,
    transaction_id                  BIGINT          NOT NULL,
    tax_code                        VARCHAR(100)    NOT NULL,
    tax_description                 VARCHAR(255)    NULL,
    tax_amount                      DECIMAL(19,4)   NOT NULL,
    tax_amount_ch                   DECIMAL(19,4)   NULL,
    category                        VARCHAR(50)     NULL,
    responsible_ministry_code       VARCHAR(100)    NULL,
    third_party_code                VARCHAR(100)    NULL,
    gol_percentage                  DECIMAL(9,6)    NULL,
    gol_amount                      DECIMAL(19,4)   NULL,
    ministry_percent                DECIMAL(9,6)    NULL,
    ministry_amount                 DECIMAL(19,4)   NULL,
    third_party_percent             DECIMAL(9,6)    NULL,
    third_party_amount              DECIMAL(19,4)   NULL,
    sending_dfsp_commission_percent DECIMAL(9,6)    NULL,
    sending_dfsp_commission_amount  DECIMAL(19,4)   NULL,
    created_date                    BIGINT          NULL,
    updated_date                    BIGINT          NULL,

    CONSTRAINT pk_tbl_transaction_detail
        PRIMARY KEY (id),

    CONSTRAINT fk_transaction_detail_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES tbl_transaction (id),

    INDEX idx_transaction_detail_transaction_id (transaction_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
