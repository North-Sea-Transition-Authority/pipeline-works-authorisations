CREATE TABLE ${datasource.user}.pad_pipeline_idents(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  pp_id NUMBER NOT NULL,
  ident_no NUMBER NOT NULL,
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
  length NUMBER,
  CONSTRAINT pad_pipeline_idents_pp_fk FOREIGN KEY (pp_id) REFERENCES ${datasource.user}.pad_pipelines (id)
);

CREATE INDEX ${datasource.user}.pad_pipeline_idents_pp_idx ON ${datasource.user}.pad_pipeline_idents (pp_id);

CREATE TABLE ${datasource.user}.pad_pipeline_ident_data(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  ppi_id NUMBER NOT NULL,
  component_parts_desc VARCHAR2(4000),
  external_diameter NUMBER,
  internal_diameter NUMBER,
  wall_thickness NUMBER,
  insulation_coating_type VARCHAR2(4000),
  maop NUMBER,
  products_to_be_conveyed VARCHAR2(4000),
  CONSTRAINT pad_pipeline_ident_data_ppi_fk FOREIGN KEY (ppi_id) REFERENCES ${datasource.user}.pad_pipeline_idents (id)
);

CREATE INDEX ${datasource.user}.pad_pipeline_ident_dat_ppi_idx ON ${datasource.user}.pad_pipeline_ident_data (ppi_id);