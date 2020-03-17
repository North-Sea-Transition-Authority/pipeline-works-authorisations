CREATE TABLE ${datasource.user}.pad_env_and_decom (
  id NUMBER GENERATED ALWAYS AS IDENTITY
, application_detail_id NUMBER
  -- Env/Decom fields
, trans_boundary_effect INTEGER CHECK(trans_boundary_effect IN (0, 1) OR trans_boundary_effect IS NULL)
, emt_has_submitted_permits INTEGER CHECK(emt_has_submitted_permits IN (0, 1) OR emt_has_submitted_permits IS NULL)
, permits_submitted VARCHAR2(4000)
, emt_has_outstanding_permits INTEGER CHECK(emt_has_outstanding_permits IN (0, 1) OR emt_has_outstanding_permits IS NULL)
, permits_pending_submission VARCHAR2(4000)
, emt_submission_timestamp TIMESTAMP
, discharge_funds_available INTEGER CHECK (discharge_funds_available IN (0, 1) OR discharge_funds_available IS NULL)
, accepts_opol_liability INTEGER CHECK (accepts_opol_liability IN (0, 1) OR accepts_opol_liability IS NULL)
, decommissioning_plans VARCHAR2(4000)
, accepts_eol_regulations INTEGER CHECK (accepts_eol_regulations IN (0, 1) OR accepts_eol_regulations IS NULL)
, accepts_eol_removal INTEGER CHECK (accepts_eol_removal IN (0, 1) OR accepts_eol_removal IS NULL)
, accepts_removal_proposal INTEGER CHECK (accepts_removal_proposal IN (0, 1) OR accepts_removal_proposal IS NULL)
, CONSTRAINT pad_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

CREATE INDEX ${datasource.user}.pad_env_and_decom_pad_idx ON ${datasource.user}.pad_env_and_decom (application_detail_id)
TABLESPACE tbsidx;