CREATE OR REPLACE VIEW ${datasource.user}.consultation_assignments
AS
SELECT
  aal.id
, aal.business_key consultation_request_id
, aal.assignment
, aal.assignee_person_id
, xrph.full_name assignee_name
, aal.assignment_timestamp
FROM ${datasource.user}.assignment_audit_log aal
JOIN decmgr.xview_resource_people_history xrph ON xrph.rp_id = aal.assignee_person_id AND xrph.status_control = 'C'
WHERE aal.workflow_type = 'PWA_APPLICATION_CONSULTATION'
AND aal.id IN (
  SELECT DISTINCT FIRST_VALUE(id) OVER (PARTITION BY business_key, workflow_type, assignment ORDER BY assignment_timestamp DESC) id
  FROM ${datasource.user}.assignment_audit_log
);