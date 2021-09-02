CREATE OR REPLACE VIEW ${datasource.user}.vw_pwa_regulator_users AS
SELECT
  p.id person_id
, p.forename || ' ' || p.surname person_name
, prmcr.role_name
FROM ${datasource.user}.portal_res_memb_current_roles prmcr
JOIN ${datasource.user}.people p on p.id = prmcr.person_id
WHERE prmcr.res_type = 'PWA_REGULATOR_TEAM';

CREATE OR REPLACE VIEW ${datasource.user}.vw_pwa_application_assignments AS
SELECT
  a.business_key pwa_application_id
, a.assignee_person_id
, a.assignment
FROM ${datasource.user}.assignments a
WHERE a.workflow_type = 'PWA_APPLICATION';