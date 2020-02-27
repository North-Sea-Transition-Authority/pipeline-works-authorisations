CREATE TABLE ${datasource.user}.pwa_details(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  pwa_id NUMBER NOT NULL,
  pwa_status VARCHAR2(4000) NOT NULL,
  reference VARCHAR2(4000) NOT NULL,
  start_timestamp TIMESTAMP NOT NULL,
  end_timestamp TIMESTAMP,
  CONSTRAINT pwad_id_fk FOREIGN KEY (pwa_id) REFERENCES ${datasource.user}.pwas (id)
) TABLESPACE tbsdata;

CREATE INDEX ${datasource.user}.pwad_fk_idx ON ${datasource.user}.pwa_details (pwa_id)
TABLESPACE tbsidx;