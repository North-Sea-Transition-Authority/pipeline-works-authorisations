CREATE TABLE ${datasource.user}.pad_pipeline_crossing_files (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, application_detail_id NUMBER NOT NULL
, file_id VARCHAR2(4000) NOT NULL
, description VARCHAR2(4000)
, file_link_status VARCHAR2(100)
, CONSTRAINT pad_pcf_pad_id_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details (id)
, CONSTRAINT pad_pcf_file_id_fk FOREIGN KEY (file_id) REFERENCES ${datasource.user}.uploaded_files (file_id)
);
CREATE INDEX ${datasource.user}.pad_pcf_pad_idx ON ${datasource.user}.pad_pipeline_crossing_files (application_detail_id);
CREATE INDEX ${datasource.user}.pad_pcf_uf_idx ON ${datasource.user}.pad_pipeline_crossing_files (file_id);

CREATE TABLE ${datasource.user}.pad_pipeline_crossings (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, application_detail_id NUMBER NOT NULL
, pipeline_crossed VARCHAR2(4000) NOT NULL
, pipeline_fully_owned_by_org INTEGER CHECK(pipeline_fully_owned_by_org IN (0, 1) OR pipeline_fully_owned_by_org IS NULL)
, CONSTRAINT pad_pipeline_crossings_pad_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);
CREATE INDEX ${datasource.user}.pad_pipeline_crossings_pad_idx ON ${datasource.user}.pad_pipeline_crossings (application_detail_id);

CREATE TABLE ${datasource.user}.pad_pipeline_crossing_owners (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, ppc_id NUMBER NOT NULL
, ou_id NUMBER
, org_manual_entry VARCHAR2(4000)
, CONSTRAINT pad_pco_ou_org_check CHECK (
    (ou_id IS NOT NULL AND org_manual_entry IS NULL)
    OR (ou_id IS NULL AND org_manual_entry IS NOT NULL)
  )
, CONSTRAINT pad_pco_ppc_fk FOREIGN KEY (ppc_id) REFERENCES ${datasource.user}.pad_pipeline_crossings(id)
);
CREATE INDEX ${datasource.user}.pad_pco_pad_ppc_idx ON ${datasource.user}.pad_pipeline_crossing_owners(ppc_id);