CREATE TABLE ${datasource.user}.pad_median_line_crossing_files (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, application_detail_id NUMBER NOT NULL
, file_id VARCHAR2(4000) NOT NULL
, description VARCHAR2(4000)
, file_link_status VARCHAR2(100)
, CONSTRAINT pad_mlcf_pad_id_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details (id)
, CONSTRAINT pad_mlcf_file_id_fk FOREIGN KEY (file_id) REFERENCES ${datasource.user}.uploaded_files (file_id)
) TABLESPACE tbsdata;

CREATE INDEX ${datasource.user}.pad_mlcf_pad_idx ON ${datasource.user}.pad_median_line_crossing_files (application_detail_id);