CREATE TABLE ${datasource.user}.case_notes (
  id NUMBER GENERATED ALWAYS AS IDENTITY,
  pwa_application_id NUMBER NOT NULL,
  person_id NUMBER NOT NULL,
  date_time TIMESTAMP NOT NULL,
  note_text CLOB,
  CONSTRAINT case_notes_pwa_app_id_fk FOREIGN KEY (pwa_application_id) REFERENCES ${datasource.user}.pwa_applications (id)
);

CREATE INDEX ${datasource.user}.case_notes_pwa_app_id_idx ON ${datasource.user}.case_notes (pwa_application_id);