
ALTER TABLE ${datasource.user}.pad_pipelines ADD (
    footnote VARCHAR2(4000)
);

ALTER TABLE ${datasource.user}.pipeline_details ADD (
    footnote VARCHAR2(4000)
);