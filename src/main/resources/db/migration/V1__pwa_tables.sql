CREATE TABLE ${datasource.user}.pwas (
  id NUMBER PRIMARY KEY,
  holder_ou_id NUMBER,
  created_timestamp TIMESTAMP NOT NULL
) TABLESPACE tbsdata;

CREATE TABLE ${datasource.user}.pwa_applications (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  pwa_id NUMBER NOT NULL,
  application_type VARCHAR2(50) NOT NULL,
  app_reference VARCHAR2(25),
  consent_reference VARCHAR2(25),
  variation_no NUMBER NOT NULL,
  decision VARCHAR2(25),
  decision_timestamp TIMESTAMP,
  CONSTRAINT pwa_fk FOREIGN KEY (pwa_id) REFERENCES ${datasource.user}.pwas (id)
) TABLESPACE tbsdata;

CREATE INDEX ${datasource.user}.pa_pwa_idx ON ${datasource.user}.pwa_applications (pwa_id)
TABLESPACE tbsidx;

CREATE TABLE ${datasource.user}.pwa_application_details (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  pwa_application_id NUMBER NOT NULL,
  tip_flag NUMBER NOT NULL,
  version_no NUMBER NOT NULL,
  status VARCHAR2(25) NOT NULL,
  created_by_wua_id NUMBER NOT NULL,
  created_timestamp TIMESTAMP NOT NULL,
  submitted_by_wua_id NUMBER,
  submitted_timestamp TIMESTAMP,
  approved_by_wua_id NUMBER,
  approved_timestamp TIMESTAMP,
  last_updated_by_wua_id NUMBER,
  last_updated_timestamp TIMESTAMP,
  CONSTRAINT pwa_app_fk FOREIGN KEY (pwa_application_id) REFERENCES ${datasource.user}.pwa_applications (id)
) TABLESPACE tbsdata;

CREATE INDEX ${datasource.user}.pwa_app_details_pa_idx ON ${datasource.user}.pwa_application_details (pwa_application_id)
TABLESPACE tbsidx;