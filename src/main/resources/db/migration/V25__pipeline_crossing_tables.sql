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
, cable_name VARCHAR2(4000)
, location VARCHAR2(4000)
, cable_owner VARCHAR2(4000)
, CONSTRAINT pad_pipeline_crossings_pad_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);
CREATE INDEX ${datasource.user}.pad_pipeline_crossings_pad_idx ON ${datasource.user}.pad_pipeline_crossings (application_detail_id);