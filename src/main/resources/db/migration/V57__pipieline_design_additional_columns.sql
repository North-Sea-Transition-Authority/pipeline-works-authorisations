ALTER TABLE ${datasource.user}.pad_pipelines ADD (
    pipeline_flexibility VARCHAR2(20),
    pipeline_material VARCHAR2(20),
    other_pipeline_material_used VARCHAR2(4000),
    pipeline_design_life INTEGER
);


