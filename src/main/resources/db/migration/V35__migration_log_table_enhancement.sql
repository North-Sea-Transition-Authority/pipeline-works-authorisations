ALTER TABLE ${datasource.user}.migrated_pipeline_auths ADD(
   migration_type VARCHAR2(4000)
,  pa_id NUMBER NOT NULL
);

CREATE TABLE ${datasource.user}.migration_master_logs (
  id  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, last_updated TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
, created_timestamp TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
, mig_master_pa_id NUMBER NOT NULL
, status VARCHAR2(4000) NOT NULL
, log_messages CLOB
, CONSTRAINT mml_uk unique (mig_master_pa_id)
) TABLESPACE tbsdata;

