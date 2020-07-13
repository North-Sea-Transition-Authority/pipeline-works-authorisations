DROP TABLE ${datasource.user}.pad_bundle_links;
DROP TABLE ${datasource.user}.pad_bundles;

ALTER TABLE ${datasource.user}.pad_pipelines
ADD (
  pipeline_in_bundle INTEGER CHECK(pipeline_in_bundle IN (0, 1))
, bundle_name VARCHAR2(4000)
);

ALTER TABLE ${datasource.user}.pipeline_details
ADD (
  pipeline_in_bundle INTEGER CHECK(pipeline_in_bundle IN (0, 1))
, bundle_name VARCHAR2(4000)
);

CREATE INDEX pad_bundle_name_idx ON ${datasource.user}.pad_pipelines(bundle_name);
CREATE INDEX pd_bundle_name_idx ON ${datasource.user}.pipeline_details(bundle_name);