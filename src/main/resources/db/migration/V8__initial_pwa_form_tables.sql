CREATE TABLE ${datasource.user}.pad_env_and_decom (
  id NUMBER GENERATED ALWAYS AS IDENTITY
, application_detail_id NUMBER
, trans_boundary_effect INTEGER CHECK(trans_boundary_effect IN (0, 1) OR trans_boundary_effect IS NULL)
, emt_has_submitted_permits INTEGER CHECK(emt_has_submitted_permits IN (0, 1) OR emt_has_submitted_permits IS NULL)
, permits_submitted VARCHAR2(4000)
, emt_has_outstanding_permits INTEGER CHECK(emt_has_outstanding_permits IN (0, 1) OR emt_has_outstanding_permits IS NULL)
, permits_pending_submission VARCHAR2(4000)
, emt_submission_timestamp TIMESTAMP
, environmental_conditions VARCHAR2(4000)
, decommissioning_plans VARCHAR2(4000)
, decommissioning_conditions VARCHAR2(4000)
, CONSTRAINT pad_env_and_decom_pad_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

CREATE INDEX ${datasource.user}.pad_env_and_decom_pad_idx ON ${datasource.user}.pad_env_and_decom (application_detail_id)
TABLESPACE tbsidx;