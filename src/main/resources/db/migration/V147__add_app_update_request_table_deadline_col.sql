
ALTER TABLE ${datasource.user}.application_update_requests ADD (
    deadline_timestamp TIMESTAMP
);

UPDATE ${datasource.user}.application_update_requests
SET deadline_timestamp = SYSTIMESTAMP;

ALTER TABLE ${datasource.user}.application_update_requests MODIFY (
    deadline_timestamp NOT NULL
);
