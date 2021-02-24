ALTER TABLE ${datasource.user}.public_notice_requests ADD(

    request_approved INTEGER CHECK (request_approved IN (0, 1))
    , rejection_reason VARCHAR2(4000)
    , ended_timestamp TIMESTAMP
);