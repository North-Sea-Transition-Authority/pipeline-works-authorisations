ALTER TABLE ${datasource.user}.pad_pipelines ADD (
  already_exists_on_seabed INTEGER CHECK(already_exists_on_seabed IN (0, 1)),
  pipeline_in_use INTEGER CHECK(pipeline_in_use IN (0, 1))
);