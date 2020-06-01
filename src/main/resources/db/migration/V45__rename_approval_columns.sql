ALTER TABLE ${datasource.user}.pwa_application_details RENAME COLUMN approved_by_wua_id TO init_review_approved_by_wua_id;

ALTER TABLE ${datasource.user}.pwa_application_details RENAME COLUMN approved_timestamp TO init_review_approved_timestamp;