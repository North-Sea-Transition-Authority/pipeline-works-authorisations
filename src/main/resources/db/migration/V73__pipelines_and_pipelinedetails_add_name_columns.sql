ALTER TABLE ${datasource.user}.pad_pipelines ADD (
    name VARCHAR2(200),
    max_external_diameter Number
);

ALTER TABLE ${datasource.user}.pipeline_details ADD (
    max_external_diameter Number
);