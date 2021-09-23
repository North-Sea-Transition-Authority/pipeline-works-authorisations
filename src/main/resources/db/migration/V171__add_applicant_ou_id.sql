ALTER TABLE ${datasource.user}.pwa_applications
ADD applicant_ou_id NUMBER;

-- set the value for existing data on the environment, any multi holder apps will get the last occurring holder
-- as the final value
DECLARE

  l_shell_ou_id NUMBER;

BEGIN

  FOR app IN (
    -- if the app has holders, use them
    SELECT pa.id, por.ou_id
    FROM ${datasource.user}.pwa_applications pa
    JOIN ${datasource.user}.pwa_application_details pad ON pad.pwa_application_id = pa.id AND pad.tip_flag = 1
    JOIN ${datasource.user}.pad_organisation_roles por ON por.application_detail_id = pad.id AND por.role = 'HOLDER'
    UNION ALL
    -- some variations don't have holders defined on them if they don't have the app task, use the consented holders instead
    SELECT pa.id, pcor.ou_id
    FROM ${datasource.user}.pwa_applications pa
    JOIN ${datasource.user}.pwa_application_details pad ON pad.pwa_application_id = pa.id AND pad.tip_flag = 1
    LEFT JOIN ${datasource.user}.pad_organisation_roles por ON por.application_detail_id = pad.id AND por.role = 'HOLDER'
    JOIN ${datasource.user}.pwa_consents pc ON pc.pwa_id = pa.pwa_id
    JOIN ${datasource.user}.pwa_consent_organisation_roles pcor on pcor.added_by_pwa_consent_id = pc.id AND pcor.ended_by_pwa_consent_id IS NULL AND pcor.role = 'HOLDER'
    WHERE por.id IS NULL
  ) LOOP
    
    UPDATE ${datasource.user}.pwa_applications
    SET applicant_ou_id = app.ou_id
    WHERE id = app.id;
    
  END LOOP;

  SELECT pou.ou_id
  INTO l_shell_ou_id
  FROM ${datasource.user}.portal_organisation_units pou
  WHERE pou.name LIKE 'SHELL%'
  AND ROWNUM = 1;

  -- for any that still don't have an applicant set, just set a hardcoded org
  FOR app IN (
    SELECT pa.id
    FROM ${datasource.user}.pwa_applications pa
    WHERE pa.applicant_ou_id IS NULL
  ) LOOP

    UPDATE ${datasource.user}.pwa_applications
    SET applicant_ou_id = l_shell_ou_id
    WHERE id = app.id;

  END LOOP;

END;
/

ALTER TABLE ${datasource.user}.pwa_applications
MODIFY applicant_ou_id NOT NULL;