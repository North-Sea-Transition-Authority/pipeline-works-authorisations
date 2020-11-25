
ALTER TABLE ${datasource.user}.pwa_application_details
  RENAME COLUMN submitted_by_wua_id TO submitted_by_person_id;

UPDATE ${datasource.user}.pwa_application_details pad
SET pad.submitted_by_person_id = (
  SELECT wua.resource_person_id
  FROM securemgr.web_user_accounts wua
  WHERE wua.id = pad.submitted_by_person_id
  )
WHERE pad.submitted_by_person_id IS NOT NULL;