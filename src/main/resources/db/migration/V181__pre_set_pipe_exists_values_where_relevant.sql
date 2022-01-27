UPDATE ${datasource.user}.pad_pipelines pp
SET
  pp.already_exists_on_seabed = 1
, pp.pipeline_in_use = 1
WHERE pp.id = (
  SELECT pp.id
  FROM ${datasource.user}.pwa_applications pa
  JOIN ${datasource.user}.pwa_application_details pad ON pad.pwa_application_id = pa.id AND pad.tip_flag = 1
  JOIN ${datasource.user}.pad_pipelines pp ON pp.pad_id = pad.id
  LEFT JOIN ${datasource.user}.pipeline_details pd ON pd.pipeline_id = pp.pipeline_id
  WHERE pa.application_type IN ('INITIAL', 'CAT_1_VARIATION', 'CAT_2_VARIATION', 'DECOMMISSIONING')
  AND pp.already_exists_on_seabed IS NULL
  AND pp.pipeline_in_use IS NULL
  AND pp.pipeline_status = 'IN_SERVICE'
  AND pd.id IS NULL -- hasn't been consented yet 
);

COMMIT;