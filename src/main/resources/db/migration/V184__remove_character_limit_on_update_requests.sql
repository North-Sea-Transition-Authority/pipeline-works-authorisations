ALTER TABLE ${datasource.user}.application_update_requests
ADD tmp_request_reason CLOB;

UPDATE ${datasource.user}.application_update_requests
SET tmp_request_reason = TO_CLOB (request_reason);

ALTER TABLE ${datasource.user}.application_update_requests
DROP COLUMN request_reason;

ALTER TABLE ${datasource.user}.application_update_requests
RENAME COLUMN tmp_request_reason TO request_reason;