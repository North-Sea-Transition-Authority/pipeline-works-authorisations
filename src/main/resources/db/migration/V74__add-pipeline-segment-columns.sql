ALTER TABLE ${datasource.user}.pipeline_org_role_links ADD (
  from_ident_id NUMBER CONSTRAINT porl_from_ident_fk REFERENCES ${datasource.user}.pipeline_detail_idents(id)
, from_ident_mode VARCHAR2(100)
, to_ident_id NUMBER CONSTRAINT porl_to_ident_fk REFERENCES ${datasource.user}.pipeline_detail_idents(id)
, to_ident_mode VARCHAR2(100)
, CONSTRAINT porl_ident_ck CHECK (
    (from_ident_id IS NULL AND from_ident_mode IS NULL AND to_ident_id IS NULL AND to_ident_mode IS NULL)
    OR
    (from_ident_id IS NOT NULL AND from_ident_mode IS NOT NULL AND to_ident_id IS NOT NULL AND to_ident_mode IS NOT NULL)
  )
);

CREATE INDEX ${datasource.user}.porl_from_ident_idx ON ${datasource.user}.pipeline_org_role_links(from_ident_id);
CREATE INDEX ${datasource.user}.porl_to_ident_idx ON ${datasource.user}.pipeline_org_role_links(to_ident_id);

ALTER TABLE ${datasource.user}.pad_pipeline_org_role_links DROP CONSTRAINT pporl_pipeline_role_unique;

ALTER TABLE ${datasource.user}.pad_pipeline_org_role_links ADD (
  from_ident_id NUMBER CONSTRAINT pporl_from_ident_fk REFERENCES ${datasource.user}.pad_pipeline_idents(id)
, from_ident_mode VARCHAR2(100)
, to_ident_id NUMBER CONSTRAINT pporl_to_ident_fk REFERENCES ${datasource.user}.pad_pipeline_idents(id)
, to_ident_mode VARCHAR2(100)
, CONSTRAINT pporl_ident_ck CHECK (
    (from_ident_id IS NULL AND from_ident_mode IS NULL AND to_ident_id IS NULL AND to_ident_mode IS NULL)
    OR
    (from_ident_id IS NOT NULL AND from_ident_mode IS NOT NULL AND to_ident_id IS NOT NULL AND to_ident_mode IS NOT NULL)
  )
);


CREATE INDEX ${datasource.user}.pporl_from_ident_idx ON ${datasource.user}.pad_pipeline_org_role_links(from_ident_id);
CREATE INDEX ${datasource.user}.pporl_to_ident_idx ON ${datasource.user}.pad_pipeline_org_role_links(to_ident_id);
