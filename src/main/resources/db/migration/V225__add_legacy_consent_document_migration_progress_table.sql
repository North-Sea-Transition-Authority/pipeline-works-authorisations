CREATE TABLE ${datasource.user}.consent_document_migration_progress(
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filename varchar2(4000),
    pwa_reference varchar2(4000),
    field_name varchar2(4000),
    consent_doc varchar2(4000),
    consent_date varchar2(4000),
    consent_type varchar2(4000),
    incorrect_pwa_reference varchar2(4000),
    action varchar2(4000),
    file_located NUMBER(1,0),
    destination_record_exists NUMBER(1,0),
    migration_successful NUMBER(1,0)
);