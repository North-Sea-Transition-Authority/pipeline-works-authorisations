CREATE OR REPLACE VIEW ${datasource.user}.consultation_search_items AS
SELECT
  pad.id tip_pad_id
, cgd.cg_id consultee_group_id
, cgd.name consultee_group_name
, cgd.abbreviation consultee_group_abbr
, cr.id consultation_request_id
, cr.deadline_date request_deadline_date
, cr.status consultation_request_status
, ca.assignee_name assigned_responder_name
FROM ${datasource.user}.consultation_requests cr
JOIN ${datasource.user}.pwa_applications pa ON pa.id = cr.application_id
JOIN ${datasource.user}.pwa_application_details pad ON pad.pwa_application_id = pa.id AND pad.tip_flag = 1
JOIN ${datasource.user}.consultee_group_details cgd ON cgd.cg_id = cr.consultee_group_id AND cgd.tip_flag = 1
LEFT JOIN ${datasource.user}.consultation_assignments ca ON ca.consultation_request_id = cr.id AND ca.assignment = 'CONSULTATION_RESPONDER';