CREATE INDEX idx_approval_request_ndc_lookup
    ON tbl_approval_request (
        request_category,
        participant_name,
        participant_currency,
        action
    );
