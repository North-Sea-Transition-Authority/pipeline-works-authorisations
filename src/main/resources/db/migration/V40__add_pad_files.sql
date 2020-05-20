CREATE TABLE ${datasource.user}.pad_files(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pad_id NUMBER NOT NULL
, file_id VARCHAR2(4000) NOT NULL
, description VARCHAR2(4000)
, purpose VARCHAR2(100) NOT NULL
, file_link_status VARCHAR2(100) NOT NULL
, CONSTRAINT pf_pad_id_fk FOREIGN KEY (pad_id) REFERENCES ${datasource.user}.pwa_application_details (id)
, CONSTRAINT pf_file_id_fk FOREIGN KEY (file_id) REFERENCES ${datasource.user}.uploaded_files (file_id)
);

CREATE INDEX ${datasource.user}.pf_pad_idx ON ${datasource.user}.pad_files (pad_id);
CREATE INDEX ${datasource.user}.pf_uf_idx ON ${datasource.user}.pad_files (file_id);

DROP TABLE ${datasource.user}.pad_location_detail_files;