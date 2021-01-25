ALTER TABLE ${datasource.user}.pwa_application_details ADD (
    deleted_timestamp TIMESTAMP,
    deleting_person_id INTEGER
);


