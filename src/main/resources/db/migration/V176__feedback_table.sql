CREATE TABLE ${datasource.user}.service_feedback (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pwa_application_detail_id NUMBER
, rating VARCHAR2(4000) NOT NULL
, feedback VARCHAR2(4000)
, submitter_name VARCHAR2(4000) NOT NULL
, submitter_email_address VARCHAR2(4000) NOT NULL
, submitted_timestamp TIMESTAMP NOT NULL
)
