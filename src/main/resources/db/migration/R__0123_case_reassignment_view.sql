CREATE OR REPLACE FORCE VIEW ${datasource.user}.case_reassignment_view AS
SELECT
    pwaa.id as application_id,
    pwaa.APP_REFERENCE as pad_reference,
    ppi.PROJECT_NAME as pad_name,
    pwad.STATUS as pad_status,
    pwad.STATUS_LAST_MODIFIED_TIMESTAMP as in_case_officer_review_since,
    paa.assignee_person_id as assigned_case_officer_person_id,
    paa.assignee_name as assigned_case_officer
FROM
    ${datasource.user}.PWA_APPLICATIONS pa
INNER JOIN ${datasource.user}.PWA_APPLICATION_DETAILS pad ON pa.id = pad.PWA_APPLICATION_ID AND tip_flag = 1
LEFT JOIN ${datasource.user}.PWA_APP_ASSIGNMENTS paa ON paa.pwa_application_id = pad.pwa_application_id AND paa.assignment = 'CASE_OFFICER'
LEFT JOIN ${datasource.user}.PAD_PROJECT_INFORMATION ppi ON ppi.application_detail_id = pad.id
WHERE pad.STATUS = 'CASE_OFFICER_REVIEW';
