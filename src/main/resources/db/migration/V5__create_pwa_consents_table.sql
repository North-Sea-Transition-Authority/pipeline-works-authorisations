CREATE TABLE ${datasource.user}.pwa_consents(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pwa_id NUMBER NOT NULL
, source_pwa_application_id NUMBER
, created_timestamp TIMESTAMP NOT NULL
, consent_timestamp TIMESTAMP NOT NULL
, consent_type VARCHAR2(4000) NOT NULL
, reference VARCHAR2(4000) NOT NULL
, variation_number NUMBER
, is_migrated_flag NUMBER(1) NOT NULL
, CONSTRAINT pwac_id_fk FOREIGN KEY (pwa_id) REFERENCES ${datasource.user}.pwas (id)
, CONSTRAINT pwac_app_id_fk FOREIGN KEY (source_pwa_application_id) REFERENCES ${datasource.user}.pwa_applications (id)
) TABLESPACE tbsdata;

CREATE INDEX ${datasource.user}.pwac_pwa_fk_idx ON ${datasource.user}.pwa_consents (pwa_id)
TABLESPACE tbsidx;

CREATE INDEX ${datasource.user}.pwac_pwaa_fk_idx ON ${datasource.user}.pwa_consents (source_pwa_application_id)
TABLESPACE tbsidx;

CREATE INDEX ${datasource.user}.pwac_type_idx ON ${datasource.user}.pwa_consents (consent_type)
TABLESPACE tbsidx;

