
ALTER TABLE ${datasource.user}.pad_deposit_pipelines RENAME COLUMN pad_pipeline_id TO pipeline_id;

ALTER TABLE ${datasource.user}.pad_deposit_pipelines ADD (
    CONSTRAINT deposit_pipeline_id_fk FOREIGN KEY(pipeline_id) REFERENCES ${datasource.user}.pipelines(id)
);