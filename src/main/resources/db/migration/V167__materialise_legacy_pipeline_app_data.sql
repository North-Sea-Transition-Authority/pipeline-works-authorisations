-- Do we even need this if Timetracker materialises this data anyway?
CREATE TABLE ${datasource.user}.materialised_legacy_app_data AS (
  SELECT
    xpad.papp_id id
  , 'PIPELINE_APPLICATION' type
  , xpad.application_reference reference
  , xpad.org_id
  , xpad.title
  , CASE
    WHEN UPPER(xpad.description) = UPPER(xpad.title) THEN NULL
    ELSE xpad.description
    END description
  , xpad.start_datetime
  , xpad.start_datetime last_accessed_datetime
  FROM decmgr.xview_pipeline_app_details xpad
  WHERE xpad.status_control = 'C'
);

