
ALTER TABLE ${datasource.user}.pad_deposit_pipelines RENAME COLUMN pad_pipeline_id TO pipeline_id;

UPDATE ${datasource.user}.pad_deposit_pipelines pdp
SET pipeline_id = (
    SELECT pp.pipeline_id
    FROM ${datasource.user}.pad_pipelines pp
    WHERE pdp.pipeline_id = pp.id
);

ALTER TABLE ${datasource.user}.pad_deposit_pipelines ADD (
    CONSTRAINT deposit_pipeline_id_fk FOREIGN KEY(pipeline_id) REFERENCES ${datasource.user}.pipelines(id)
);