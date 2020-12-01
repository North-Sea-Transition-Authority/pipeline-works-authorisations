ALTER TABLE ${datasource.user}.pwa_application_details ADD (

    withdrawal_timestamp TIMESTAMP,
    withdrawal_reason VARCHAR2(4000),
    withdrawing_person_id INTEGER
);

