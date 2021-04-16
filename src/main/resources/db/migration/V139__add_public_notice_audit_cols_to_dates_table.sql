
ALTER TABLE ${datasource.user}.public_notice_dates ADD (
    created_timestamp TIMESTAMP,
    ended_timestamp TIMESTAMP
);
