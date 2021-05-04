ALTER TABLE ${datasource.user}.pwa_applications ADD app_created_timestamp TIMESTAMP;

UPDATE ${datasource.user}.pwa_applications pa
  SET pa.app_created_timestamp = (
    SELECT MIN(pad.created_timestamp)
    FROM ${datasource.user}.pwa_application_details pad
    WHERE pad.pwa_application_id = pa.id
  );

ALTER TABLE ${datasource.user}.pwa_applications MODIFY app_created_timestamp TIMESTAMP NOT NULL;