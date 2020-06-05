
CREATE TABLE ${datasource.user}.pipeline_detail_migr_huoo_data (
  id NUMBER GENERATED AS IDENTITY PRIMARY KEY
, pipeline_detail_id NUMBER NOT NULL
  CONSTRAINT pdmhd_fk REFERENCES ${datasource.user}.pipeline_details(id)
, organisation_role VARCHAR2(8) CONSTRAINT pdmhd_role_ck CHECK(organisation_role IN ('HOLDER', 'USER', 'OPERATOR', 'OWNER'))
, organisation_unit_id NUMBER
, manual_organisation_name VARCHAR2(4000)
, CONSTRAINT pdmhd_org_ck CHECK(
  (organisation_unit_id IS NULL AND manual_organisation_name IS NOT NULL)
    OR ((organisation_unit_id IS NOT NULL AND manual_organisation_name IS NULL))
  )
);

CREATE INDEX ${datasource.user}.pdmhd_pl_detail_fk_idx ON ${datasource.user}.pipeline_detail_migr_huoo_data(pipeline_detail_id);


CREATE TABLE ${datasource.user}.pwa_consent_organisation_roles (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, added_by_pwa_consent_id NUMBER NOT NULL CONSTRAINT pcor_add_consent_fk REFERENCES ${datasource.user}.pwa_consents(id)
, role VARCHAR2(8) NOT NULL CONSTRAINT pcor_role_ck CHECK(role IN ('HOLDER', 'USER', 'OPERATOR', 'OWNER'))
, type VARCHAR2(4000) NOT NULL
, ou_id NUMBER
, migrated_organisation_name VARCHAR2(4000)
, agreement VARCHAR2(4000)
, start_timestamp TIMESTAMP NOT NULL
, end_timestamp TIMESTAMP
, ended_by_pwa_consent_id NUMBER CONSTRAINT pcor_end_consent_fk REFERENCES ${datasource.user}.pwa_consents(id)
, CONSTRAINT pcor_ended_ck CHECK(
    (ended_by_pwa_consent_id IS NULL AND end_timestamp IS NULL)
  OR ((ended_by_pwa_consent_id IS NOT NULL AND end_timestamp IS NOT NULL))
  )
, CONSTRAINT pcor_role_owner_check CHECK (
    (ou_id IS NULL AND agreement IS NOT NULL)
    OR (ou_id IS NOT NULL AND agreement IS NULL)
    OR (migrated_organisation_name IS NOT NULL AND ou_id IS NULL AND agreement IS NULL) -- reference migration data support
  )
);

CREATE INDEX ${datasource.user}.pcor_added_consent_fk_idx ON ${datasource.user}.pwa_consent_organisation_roles(added_by_pwa_consent_id);
CREATE INDEX ${datasource.user}.pcor_ended_consent_fk_idx ON ${datasource.user}.pwa_consent_organisation_roles(ended_by_pwa_consent_id);


CREATE TABLE ${datasource.user}.pipeline_org_role_links (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pipeline_id NUMBER NOT NULL CONSTRAINT porl_pipeline_fk REFERENCES ${datasource.user}.pipelines(id)
, pwa_consent_org_role_id NUMBER NOT NULL CONSTRAINT porl_consent_fk REFERENCES ${datasource.user}.pwa_consent_organisation_roles(id)
, added_by_pwa_consent_id NUMBER NOT NULL CONSTRAINT porl_add_consent_fk REFERENCES ${datasource.user}.pwa_consents(id)
, ended_by_pwa_consent_id NUMBER CONSTRAINT porl_end_consent_fk REFERENCES ${datasource.user}.pwa_consents(id)
, start_timestamp TIMESTAMP NOT NULL
, end_timestamp TIMESTAMP
, CONSTRAINT porl_ended_ck CHECK(
    (ended_by_pwa_consent_id IS NULL AND end_timestamp IS NULL)
    OR ((ended_by_pwa_consent_id IS NOT NULL AND end_timestamp IS NOT NULL))
  )
);

CREATE INDEX ${datasource.user}.porl_pipeline_fk_idx ON ${datasource.user}.pipeline_org_role_links(pipeline_id);
CREATE INDEX ${datasource.user}.porl_consent_org_role_fk_idx ON ${datasource.user}.pipeline_org_role_links(pwa_consent_org_role_id);
CREATE INDEX ${datasource.user}.porl_added_consent_fk_idx ON ${datasource.user}.pipeline_org_role_links(added_by_pwa_consent_id);
CREATE INDEX ${datasource.user}.porl_ended_consent_fk_idx ON ${datasource.user}.pipeline_org_role_links(ended_by_pwa_consent_id);