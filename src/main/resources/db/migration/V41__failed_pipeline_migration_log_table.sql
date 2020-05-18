CREATE TABLE ${datasource.user}.migration_pipeline_logs (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, last_updated TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
, created_timestamp TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
, master_pipeline_created_flag VARCHAR2(1)
, pipeline_id NUMBER NOT NULL
, pipeline_detail_id NUMBER NOT NULL
, pad_id NUMBER
, status VARCHAR2(4000) NOT NULL
, log_messages CLOB
, CONSTRAINT mpl_unq UNIQUE (pipeline_detail_id)
) TABLESPACE tbsdata;
