CREATE TABLE ${datasource.user}.assignments (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  business_key NUMBER NOT NULL,
  workflow_type VARCHAR2(50) NOT NULL,
  assignment VARCHAR2(50) NOT NULL,
  assignee_person_id NUMBER NOT NULL
);

CREATE INDEX ${datasource.user}.assignments_bkey_wtype_idx ON ${datasource.user}.assignments (business_key, workflow_type, assignment);

CREATE INDEX ${datasource.user}.assignments_person_id_idx ON ${datasource.user}.assignments (assignee_person_id);

-- populate assignments table from audit log as first pass (will only do something on dev/st/uat, live tables will be empty)
INSERT INTO ${datasource.user}.assignments (business_key, workflow_type, assignment, assignee_person_id)
SELECT
  aal.business_key
, aal.workflow_type
, aal.assignment
, aal.assignee_person_id
FROM ${datasource.user}.assignment_audit_log aal
JOIN decmgr.xview_resource_people_history xrph ON xrph.rp_id = aal.assignee_person_id AND xrph.status_control = 'C'
JOIN ${datasource.user}.pwa_application_details pad ON pad.pwa_application_id = aal.business_key AND pad.tip_flag = 1 AND pad.status NOT IN ('WITHDRAWN', 'DELETED', 'COMPLETE')
WHERE aal.workflow_type = 'PWA_APPLICATION'
AND aal.id IN (
  SELECT DISTINCT FIRST_VALUE(id) OVER (PARTITION BY business_key, workflow_type, assignment ORDER BY assignment_timestamp DESC) id
  FROM ${datasource.user}.assignment_audit_log
);

INSERT INTO ${datasource.user}.assignments (business_key, workflow_type, assignment, assignee_person_id)
SELECT
  aal.business_key
, aal.workflow_type
, aal.assignment
, aal.assignee_person_id
FROM ${datasource.user}.assignment_audit_log aal
JOIN decmgr.xview_resource_people_history xrph ON xrph.rp_id = aal.assignee_person_id AND xrph.status_control = 'C'
JOIN ${datasource.user}.consultation_requests cr ON cr.id = aal.business_key AND cr.status = 'AWAITING_RESPONSE'
WHERE aal.workflow_type = 'PWA_APPLICATION_CONSULTATION'
AND aal.id IN (
  SELECT DISTINCT FIRST_VALUE(id) OVER (PARTITION BY business_key, workflow_type, assignment ORDER BY assignment_timestamp DESC) id
  FROM ${datasource.user}.assignment_audit_log
);