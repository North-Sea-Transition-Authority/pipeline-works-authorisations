
ALTER TABLE ${datasource.user}.pwa_application_details
  RENAME COLUMN submitted_by_wua_id TO submitted_by_person_id;

UPDATE ${datasource.user}.pwa_application_details pad
SET pad.submitted_by_person_id = (
  SELECT ua.person_id
  FROM ${datasource.user}.user_accounts ua
  WHERE ua.wua_id = pad.submitted_by_person_id
  )
WHERE pad.submitted_by_person_id IS NOT NULL;