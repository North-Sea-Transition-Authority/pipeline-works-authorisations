CREATE TABLE ${datasource.user}.pad_organisation_roles (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, application_detail_id NUMBER NOT NULL
, ou_id NUMBER
, role VARCHAR2(4000) NOT NULL
, type VARCHAR2(4000) NOT NULL
, agreement VARCHAR2(4000)
, CONSTRAINT pad_organisation_roles_pad_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
, CONSTRAINT pad_organisation_roles_check CHECK (
    (ou_id IS NULL AND agreement IS NOT NULL)
    OR (ou_id IS NOT NULL AND agreement IS NULL)
    OR (ou_id IS NULL AND agreement IS NOT NULL)
  )
)