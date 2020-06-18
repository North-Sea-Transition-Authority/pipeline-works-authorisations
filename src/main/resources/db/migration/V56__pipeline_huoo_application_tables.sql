
ALTER TABLE ${datasource.user}.pad_pipelines MODIFY (
  pipeline_id NUMBER NOT NULL
);


CREATE TABLE ${datasource.user}.pad_pipeline_org_role_links (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pipeline_id NUMBER NOT NULL CONSTRAINT pporl_pipeline_fk REFERENCES ${datasource.user}.pipelines(id)
, pad_org_role_id NUMBER NOT NULL CONSTRAINT pporl_pad_org_role_fk REFERENCES ${datasource.user}.pad_organisation_roles(id)
);

CREATE INDEX ${datasource.user}.pporl_pipeline_fk_idx ON ${datasource.user}.pad_pipeline_org_role_links(pipeline_id);
CREATE INDEX ${datasource.user}.pporl_pad_org_role_fk_idx ON ${datasource.user}.pad_pipeline_org_role_links(pad_org_role_id);