-- ${datasource.user}
CREATE TABLE ${datasource.user}.pad_blocks (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, application_detail_id NUMBER NOT NULL
, plm_id NUMBER
, block_ref VARCHAR2(4000)
, quadrant_no VARCHAR2(4000)
, block_no VARCHAR2(4000)
, suffix VARCHAR2(4000)
, location VARCHAR2(4000)
, licence_status VARCHAR2(4000)
, created_timestamp TIMESTAMP
, CONSTRAINT pad_blocks_pad_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
) TABLESPACE tbsdata;

CREATE INDEX ${datasource.user}.pad_blocks_pad_idx ON ${datasource.user}.pad_blocks (application_detail_id)
TABLESPACE tbsidx;