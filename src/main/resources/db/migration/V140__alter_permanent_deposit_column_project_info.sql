ALTER TABLE ${datasource.user}.pad_project_information ADD permanent_deposits_made_new VARCHAR2(16);
/

UPDATE ${datasource.user}.pad_project_information pad_project_info
SET permanent_deposits_made_new = CASE
    WHEN permanent_deposits_made = 0 THEN 'NONE'
    WHEN permanent_deposits_made = 1 AND (
                                             SELECT pwa_app.application_type
                                             FROM ${datasource.user}.pwa_applications pwa_app
                                             WHERE pwa_app.id = pad_project_info.application_detail_id
                                         ) = 'OPTIONS_VARIATION' THEN 'YES'
    WHEN permanent_deposits_made = 1 AND future_app_submission_month IS NULL THEN 'THIS_APP'
    WHEN permanent_deposits_made = 1 AND future_app_submission_month IS NOT NULL THEN 'LATER_APP'
    ELSE NULL
    END;
/

ALTER TABLE ${datasource.user}.pad_project_information DROP COLUMN permanent_deposits_made;
/

ALTER TABLE ${datasource.user}.pad_project_information RENAME COLUMN permanent_deposits_made_new TO permanent_deposits_made;
/