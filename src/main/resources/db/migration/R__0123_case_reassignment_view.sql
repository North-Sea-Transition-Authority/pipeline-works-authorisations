CREATE OR REPLACE FORCE VIEW ${datasource.user}.case_reassignment_view AS
SELECT
    pwaa.id as pad_id,
    pwaa.APP_REFERENCE as pad_reference,
    pwad.STATUS as pad_status,
    pwad.STATUS_LAST_MODIFIED_TIMESTAMP as in_case_officer_review_since,
    paa.assignee_person_id as assigned_case_officer_person_id,
    paa.assignee_name as assigned_case_officer
FROM
    ${datasource.user}.PWA_APPLICATIONS pwaa
JOIN ${datasource.user}.PWA_APPLICATION_DETAILS pwad ON pwaa.id = pwad.PWA_APPLICATION_ID
JOIN ${datasource.user}.pwa_app_assignments paa ON paa.pwa_application_id = pwad.pwa_application_id AND paa.assignment = 'CASE_OFFICER'
WHERE pwad.STATUS = 'CASE_OFFICER_REVIEW';
