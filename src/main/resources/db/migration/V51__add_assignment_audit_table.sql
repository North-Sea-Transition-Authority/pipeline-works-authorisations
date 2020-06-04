CREATE TABLE ${datasource.user}.assignment_audit_log (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  business_key NUMBER NOT NULL,
  workflow_type VARCHAR2(4000) NOT NULL,
  task_key VARCHAR2(4000) NOT NULL,
  assignment VARCHAR2(4000) NOT NULL,
  assignee_person_id NUMBER NOT NULL,
  assigned_by_person_id NUMBER NOT NULL,
  assignment_timestamp TIMESTAMP NOT NULL
);

CREATE INDEX ${datasource.user}.assignment_audit_log_idx1 ON ${datasource.user}.assignment_audit_log (business_key, workflow_type);