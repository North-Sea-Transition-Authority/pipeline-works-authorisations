CREATE TABLE ${datasource.user}.pwa_application_events (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pwa_application_id NUMBER NOT NULL
, event_type VARCHAR2(4000) NOT NULL
, event_timestamp TIMESTAMP NOT NULL
, event_cleared_timestamp TIMESTAMP
, message VARCHAR2(4000)
, event_wua_id NUMBER NOT NULL
, CONSTRAINT pae_pa_id_fk FOREIGN KEY (pwa_application_id) REFERENCES ${datasource.user}.pwa_applications (id)
);

CREATE INDEX ${datasource.user}.pae_pa_id_idx ON ${datasource.user}.pwa_application_events (pwa_application_id);