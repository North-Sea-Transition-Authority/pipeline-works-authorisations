CREATE TABLE ${datasource.user}.pad_holders(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  pad_id NUMBER NOT NULL,
  holder_ou_id NUMBER NOT NULL,
  CONSTRAINT pad_id_fk FOREIGN KEY (pad_id) REFERENCES ${datasource.user}.pwa_application_details (id)
) TABLESPACE tbsdata;

CREATE INDEX ${datasource.user}.pad_holders_idx1 ON ${datasource.user}.pad_holders (pad_id)
TABLESPACE tbsidx;