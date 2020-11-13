CREATE TABLE ${datasource.user}.options_application_approvals (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, created_by_person_id NUMBER NOT NULL
, created_timestamp TIMESTAMP NOT NULL
, pwa_application_id NUMBER NOT NULL
, CONSTRAINT oaa_pwa_app_id_fk FOREIGN KEY (pwa_application_id) REFERENCES ${datasource.user}.pwa_applications(id)
);

CREATE INDEX ${datasource.user}.oaa_pwa_app_id_idx ON ${datasource.user}.options_application_approvals(pwa_application_id);

CREATE TABLE ${datasource.user}.options_app_appr_deadline_hist (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, options_app_approval_id NUMBER NOT NULL
, CONSTRAINT oaadh_oaa_id_fk FOREIGN KEY (options_app_approval_id) REFERENCES ${datasource.user}.options_application_approvals(id)
, created_by_person_id NUMBER NOT NULL
, created_timestamp TIMESTAMP NOT NULL
, deadline_date TIMESTAMP NOT NULL
, note VARCHAR2(4000)
, tip_flag INTEGER NOT NULL
, CONSTRAINT oaahd_tip_ck CHECK(tip_flag IN (0, 1))
);

CREATE INDEX ${datasource.user}.oaahd_oaa_id_idx ON ${datasource.user}.options_app_appr_deadline_hist(options_app_approval_id);