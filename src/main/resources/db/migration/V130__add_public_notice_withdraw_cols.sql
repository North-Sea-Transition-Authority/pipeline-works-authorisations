ALTER TABLE ${datasource.user}.public_notices ADD(

    withdrawing_person_id INTEGER
    , withdrawal_reason VARCHAR2(4000)
    , withdrawal_timestamp TIMESTAMP
);
