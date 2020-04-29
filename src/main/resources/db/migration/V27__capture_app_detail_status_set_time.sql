-- unused columns so far with no specific purpose.
ALTER TABLE ${datasource.user}.pwa_application_details DROP (
  last_updated_timestamp
, last_updated_by_wua_id
);

ALTER TABLE ${datasource.user}.pwa_application_details ADD (
  status_last_modified_timestamp TIMESTAMP
, status_last_modified_by_wua_id INTEGER
);

UPDATE ${datasource.user}.pwa_application_details pad
SET pad.status_last_modified_timestamp = COALESCE (pad.submitted_timestamp, pad.created_timestamp)
, pad.status_last_modified_by_wua_id = COALESCE (pad.submitted_by_wua_id, pad.created_by_wua_id);

ALTER TABLE ${datasource.user}.pwa_application_details MODIFY (
  status_last_modified_timestamp TIMESTAMP NOT NULL
, status_last_modified_by_wua_id INTEGER NOT NULL
);