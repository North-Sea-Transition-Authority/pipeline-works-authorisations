ALTER TABLE ${datasource.user}.permanent_deposit_information ADD (
    reference VARCHAR2(50) UNIQUE
);