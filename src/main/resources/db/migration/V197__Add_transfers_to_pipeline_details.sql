ALTER TABLE ${datasource.user}.pipeline_details ADD transferred_from_pipeline_id NUMBER;
ALTER TABLE ${datasource.user}.pipeline_details ADD transferred_to_pipeline_id NUMBER;

ALTER TABLE ${datasource.user}.pipeline_details ADD CONSTRAINT transferred_from_pipe_id_fk FOREIGN KEY (transferred_from_pipeline_id) REFERENCES ${datasource.user}.pipelines (id);
ALTER TABLE ${datasource.user}.pipeline_details ADD CONSTRAINT transferred_to_pipe_id_fk FOREIGN KEY (transferred_to_pipeline_id) REFERENCES ${datasource.user}.pipelines (id);

CREATE INDEX ${datasource.user}.pd_transf_from_pipeline_id_idx ON ${datasource.user}.pipeline_details (transferred_from_pipeline_id);
CREATE INDEX ${datasource.user}.pd_transf_to_pipeline_id_idx ON ${datasource.user}.pipeline_details (transferred_to_pipeline_id);