CREATE TABLE ${datasource.user}.pipelines(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  pwa_id NUMBER NOT NULL,
  CONSTRAINT pipelines_master_pwa_fk FOREIGN KEY (pwa_id) REFERENCES ${datasource.user}.pwas (id)
);

CREATE SEQUENCE ${datasource.user}.pipeline_id_seq
START WITH 1
INCREMENT BY 1
NOCACHE;

CREATE INDEX ${datasource.user}.pipelines_master_pwa_id_idx ON ${datasource.user}.pipelines (pwa_id);

CREATE TABLE ${datasource.user}.pad_pipelines(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  pad_id NUMBER NOT NULL,
  pipeline_id NUMBER,
  pipeline_type VARCHAR2(100) NOT NULL,
  from_location VARCHAR2(500),
  from_lat_deg NUMBER,
  from_lat_min NUMBER,
  from_lat_sec NUMBER,
  from_lat_dir VARCHAR2(5),
  from_long_deg NUMBER,
  from_long_min NUMBER,
  from_long_sec NUMBER,
  from_long_dir VARCHAR2(5),
  to_location VARCHAR2(500),
  to_lat_deg NUMBER,
  to_lat_min NUMBER,
  to_lat_sec NUMBER,
  to_lat_dir VARCHAR2(5),
  to_long_deg NUMBER,
  to_long_min NUMBER,
  to_long_sec NUMBER,
  to_long_dir VARCHAR2(5),
  component_parts_desc VARCHAR2(4000),
  length NUMBER,
  products_to_be_conveyed VARCHAR2(4000),
  trenched_buried_filled_flag NUMBER,
  trenching_methods_desc VARCHAR2(4000),
  CONSTRAINT pad_pipelines_pad_fk FOREIGN KEY (pad_id) REFERENCES ${datasource.user}.pwa_application_details (id),
  CONSTRAINT pad_pipelines_pipe_fk FOREIGN KEY (pipeline_id) REFERENCES ${datasource.user}.pipelines (id),
  CONSTRAINT pad_pipelines_trench_flag_ck CHECK (trenched_buried_filled_flag IN (0, 1) OR trenched_buried_filled_flag IS NULL)
);

CREATE INDEX ${datasource.user}.pad_pipelines_pad_idx ON ${datasource.user}.pad_pipelines (pad_id);

CREATE INDEX ${datasource.user}.pad_pipelines_pipe_idx ON ${datasource.user}.pad_pipelines (pipeline_id);