CREATE TABLE IF NOT EXISTS tbl_revenue_party (
    revenue_party_id BIGINT NOT NULL,
    party_code       VARCHAR(50) NOT NULL,
    party_name       VARCHAR(255) NOT NULL,
    party_type       VARCHAR(50) NOT NULL,
    description      VARCHAR(500) NULL,
    is_active        BIT(1) NOT NULL DEFAULT b'1',
    created_by       BIGINT NULL,
    updated_by       BIGINT NULL,
    created_date     BIGINT NULL,
    updated_date     BIGINT NULL,

    CONSTRAINT pk_tbl_revenue_party
        PRIMARY KEY (revenue_party_id),

    CONSTRAINT uk_revenue_party_code
        UNIQUE (party_code),

    INDEX idx_revenue_party_name (party_name),
    INDEX idx_revenue_party_type (party_type),
    INDEX idx_revenue_party_active (is_active)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

  CREATE TABLE IF NOT EXISTS tbl_revenue_party_history (
      revenue_party_history_id BIGINT NOT NULL,
      revenue_party_id         BIGINT NOT NULL,
      party_code               VARCHAR(50) NOT NULL,
      party_name               VARCHAR(255) NOT NULL,
      party_type               VARCHAR(50) NOT NULL,
      description              VARCHAR(500) NULL,
      is_active                BIT(1) NOT NULL,
      created_by               BIGINT NULL,
      created_date             BIGINT NULL,
      action_type              VARCHAR(20) NOT NULL,
      changed_by               BIGINT NULL,
      changed_date             BIGINT NULL,

      CONSTRAINT pk_revenue_party_history
          PRIMARY KEY (revenue_party_history_id),

      CONSTRAINT fk_party_history_revenue_party
          FOREIGN KEY (revenue_party_id)
          REFERENCES tbl_revenue_party (revenue_party_id),

      CONSTRAINT chk_party_history_action_type
          CHECK (
              action_type IN (
                  'CREATE',
                  'UPDATE',
                  'ACTIVATE',
                  'DEACTIVATE'
              )
          ),

      INDEX idx_party_history_party_id (revenue_party_id),
      INDEX idx_party_history_action_type (action_type),
      INDEX idx_party_history_changed_date (changed_date)
  ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;