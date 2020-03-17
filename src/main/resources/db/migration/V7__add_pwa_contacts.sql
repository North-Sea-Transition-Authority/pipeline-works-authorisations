CREATE TABLE ${datasource.user}.pwa_application_contacts(
  id NUMBER GENERATED ALWAYS AS IDENTITY,
  pwa_application_id NUMBER NOT NULL,
  person_id NUMBER NOT NULL,
  csv_role_list VARCHAR2(4000),
  CONSTRAINT pwa_contacts_pwa_app_id_fk FOREIGN KEY (pwa_application_id) REFERENCES ${datasource.user}.pwa_applications (id),
  CONSTRAINT pwa_contacts_uniq_pwa_person UNIQUE (pwa_application_id, person_id) USING INDEX TABLESPACE tbsidx
) TABLESPACE tbsdata;

CREATE INDEX ${datasource.user}.pwa_contacts_pwa_id_idx ON ${datasource.user}.pwa_application_contacts (pwa_application_id)
TABLESPACE tbsidx;