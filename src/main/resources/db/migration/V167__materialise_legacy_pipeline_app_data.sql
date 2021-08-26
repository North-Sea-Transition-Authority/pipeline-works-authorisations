
CREATE TABLE ${datasource.user}.materialised_legacy_app_data AS (
  SELECT xpad.*
  FROM decmgr.xview_pipeline_app_details xpad
  WHERE xpad.status_control = 'C'
);

