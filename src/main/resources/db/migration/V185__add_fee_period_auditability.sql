ALTER TABLE ${datasource.user}.fee_periods
    ADD created TIMESTAMP
    ADD last_modified TIMESTAMP;

ALTER TABLE ${datasource.user}.fee_period_details
    ADD last_modified_by_person_id NUMBER
    ADD created TIMESTAMP
    ADD last_modified TIMESTAMP;

ALTER TABLE ${datasource.user}.fee_period_detail_fee_items
    ADD created TIMESTAMP
    ADD last_modified TIMESTAMP;