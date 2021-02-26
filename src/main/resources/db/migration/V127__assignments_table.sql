CREATE TABLE ${datasource.user}.assignments (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  business_key NUMBER NOT NULL,
  workflow_type VARCHAR2(4000) NOT NULL,
  assignment VARCHAR2(4000) NOT NULL,
  assignee_person_id NUMBER NOT NULL
);

CREATE INDEX ${datasource.user}.assignments_bkey_wtype_idx ON ${datasource.user}.assignments (business_key, workflow_type);

CREATE INDEX ${datasource.user}.assignments_person_id_idx ON ${datasource.user}.assignments (assignee_person_id);