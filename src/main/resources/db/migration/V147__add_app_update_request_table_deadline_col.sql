
ALTER TABLE ${datasource.user}.application_update_requests ADD (
    deadline_timestamp TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);
