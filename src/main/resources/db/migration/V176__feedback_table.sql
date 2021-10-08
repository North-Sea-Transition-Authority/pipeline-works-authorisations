CREATE TABLE ${datasource.user}.service_feedback (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pwa_application_detail_id NUMBER
, rating VARCHAR2(4000) NOT NULL
, feedback VARCHAR2(4000)
, submitter_name VARCHAR2(4000) NOT NULL
, submitter_email_address VARCHAR2(4000) NOT NULL
, submitted_timestamp TIMESTAMP NOT NULL

, CONSTRAINT sf_pad_id_fk FOREIGN KEY (pwa_application_detail_id) REFERENCES ${datasource.user}.pwa_application_details (id)
);

CREATE INDEX ${datasource.user}.sf_pad_id_idx ON ${datasource.user}.service_feedback (pwa_application_detail_id);