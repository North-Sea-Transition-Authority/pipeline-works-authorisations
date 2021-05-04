
ALTER TABLE ${datasource.user}.public_notice_dates ADD (
    created_timestamp TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    ended_timestamp TIMESTAMP
    );
