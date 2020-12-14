ALTER TABLE ${datasource.user}.pipeline_detail_migration_data ADD (
  detail_status VARCHAR2(4000)
, pipeline_status VARCHAR2(4000)
);

ALTER TABLE ${datasource.user}.pipeline_details DROP COLUMN detail_status;