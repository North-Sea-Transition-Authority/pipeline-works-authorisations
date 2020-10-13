ALTER TABLE ${datasource.user}.application_update_requests ADD status VARCHAR2(50);

UPDATE ${datasource.user}.application_update_requests
SET status = 'OPEN';

ALTER TABLE ${datasource.user}.application_update_requests MODIFY status NOT NULL;