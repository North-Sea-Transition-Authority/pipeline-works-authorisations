CREATE TABLE ${datasource.user}.migrated_pipeline_auths(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  pad_id NUMBER NOT NULL,
  pwa_pipeline_consent_id NUMBER NOT NULL,
  migrated_timestamp TIMESTAMP NOT NULL
) TABLESPACE tbsdata;

-- Hard enforce a rule which prevents double migration of a particular pad_id
CREATE UNIQUE INDEX ${datasource.user}.mpa_idx1 ON ${datasource.user}.migrated_pipeline_auths (pad_id)
TABLESPACE tbsidx;

-- Hard enforce a rule which prevents double usage of a particular consent
CREATE UNIQUE INDEX ${datasource.user}.mpa_idx2 ON ${datasource.user}.migrated_pipeline_auths (pwa_pipeline_consent_id)
  TABLESPACE tbsidx;



