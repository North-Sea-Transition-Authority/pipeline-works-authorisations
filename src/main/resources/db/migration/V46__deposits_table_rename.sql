ALTER TABLE ${datasource.user}.permanent_deposit_information rename to
    pad_permanent_deposits;

ALTER TABLE ${datasource.user}.pad_permanent_deposits ADD (
    reference VARCHAR2(50)
    );

