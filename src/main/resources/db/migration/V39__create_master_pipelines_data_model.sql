CREATE TABLE ${datasource.user}.pipeline_details (
  id NUMBER PRIMARY KEY
, pipeline_id NUMBER NOT NULL
    CONSTRAINT pd_pipeline_fk REFERENCES ${datasource.user}.pipelines(id)
, start_timestamp TIMESTAMP NOT NULL
, end_timestamp TIMESTAMP
, tip_flag INTEGER
, CONSTRAINT pd_tip_flag_ck CHECK(tip_flag IN (0, 1) OR tip_flag IS NULL)
, pipeline_status VARCHAR2(100)
, detail_status VARCHAR2(100)
, pipeline_reference VARCHAR2(100)

, pipeline_type VARCHAR2(100)
, from_location VARCHAR2(500)
, from_lat_deg NUMBER
, from_lat_min NUMBER
, from_lat_sec NUMBER
, from_lat_dir VARCHAR2(5)
, CONSTRAINT pd_from_lat_ck CHECK(
    (from_lat_deg IS NOT NULL AND from_lat_min IS NOT NULL AND from_lat_sec IS NOT NULL AND from_lat_dir IS NOT NULL)
    OR
    (from_lat_deg IS NULL AND from_lat_min IS NULL AND from_lat_sec IS NULL AND from_lat_dir IS NULL)
  )
, from_long_deg NUMBER
, from_long_min NUMBER
, from_long_sec NUMBER
, from_long_dir VARCHAR2(5)
, CONSTRAINT pd_from_long_ck CHECK(
    (from_long_deg IS NOT NULL AND from_long_min IS NOT NULL AND from_long_sec IS NOT NULL AND from_long_dir IS NOT NULL)
    OR
    (from_long_deg IS NULL AND from_long_min IS NULL AND from_long_sec IS NULL AND from_long_dir IS NULL)
   )
, to_location VARCHAR2(500)
, to_lat_deg NUMBER
, to_lat_min NUMBER
, to_lat_sec NUMBER
, to_lat_dir VARCHAR2(5)
, CONSTRAINT pd_to_lat_ck CHECK(
    (to_lat_deg IS NOT NULL AND to_lat_min IS NOT NULL AND to_lat_sec IS NOT NULL AND to_lat_dir IS NOT NULL)
    OR
    (to_lat_deg IS NULL AND to_lat_min IS NULL AND to_lat_sec IS NULL AND to_lat_dir IS NULL)
  )
, to_long_deg NUMBER
, to_long_min NUMBER
, to_long_sec NUMBER
, to_long_dir VARCHAR2(5)
, CONSTRAINT pd_to_long_ck CHECK(
    (to_long_deg IS NOT NULL AND to_long_min IS NOT NULL AND to_long_sec IS NOT NULL AND to_long_dir IS NOT NULL)
    OR
    (to_long_deg IS NULL AND to_long_min IS NULL AND to_long_sec IS NULL AND to_long_dir IS NULL)
  )
, component_parts_desc VARCHAR2(4000)
, length NUMBER,
  products_to_be_conveyed VARCHAR2(4000)
, trenched_buried_filled_flag NUMBER
, CONSTRAINT pd_trench_flag_ck CHECK (trenched_buried_filled_flag IN (0, 1) OR trenched_buried_filled_flag IS NULL)
, trenching_methods_desc VARCHAR2(4000)
);

CREATE SEQUENCE ${datasource.user}.pipeline_details_id_seq
START WITH 1
INCREMENT BY 1
NOCACHE;

CREATE INDEX ${datasource.user}.pd_pipeline_idx ON ${datasource.user}.pipeline_details(pipeline_id);

CREATE TABLE ${datasource.user}.pipeline_detail_idents (
  id NUMBER GENERATED AS IDENTITY PRIMARY KEY,
  pipeline_detail_id NUMBER NOT NULL
    CONSTRAINT pdi_pipeline_detail_fk REFERENCES ${datasource.user}.pipeline_details(id)
, ident_no NUMBER NOT NULL
, from_location VARCHAR2(500)
, from_lat_deg NUMBER
, from_lat_min NUMBER
, from_lat_sec NUMBER
, from_lat_dir VARCHAR2(5)
, CONSTRAINT pdi_from_lat_ck CHECK(
    (from_lat_deg IS NOT NULL AND from_lat_min IS NOT NULL AND from_lat_sec IS NOT NULL AND from_lat_dir IS NOT NULL)
    OR
    (from_lat_deg IS NULL AND from_lat_min IS NULL AND from_lat_sec IS NULL AND from_lat_dir IS NULL)
  )
, from_long_deg NUMBER
, from_long_min NUMBER
, from_long_sec NUMBER
, from_long_dir VARCHAR2(5)
, CONSTRAINT pdi_from_long_ck CHECK(
    (from_long_deg IS NOT NULL AND from_long_min IS NOT NULL AND from_long_sec IS NOT NULL AND from_long_dir IS NOT NULL)
    OR
    (from_long_deg IS NULL AND from_long_min IS NULL AND from_long_sec IS NULL AND from_long_dir IS NULL)
  )
, to_location VARCHAR2(500)
, to_lat_deg NUMBER
, to_lat_min NUMBER
, to_lat_sec NUMBER
, to_lat_dir VARCHAR2(5)
, CONSTRAINT pdi_to_lat_ck CHECK(
    (to_lat_deg IS NOT NULL AND to_lat_min IS NOT NULL AND to_lat_sec IS NOT NULL AND to_lat_dir IS NOT NULL)
    OR
    (to_lat_deg IS NULL AND to_lat_min IS NULL AND to_lat_sec IS NULL AND to_lat_dir IS NULL)
  )
, to_long_deg NUMBER
, to_long_min NUMBER
, to_long_sec NUMBER
, to_long_dir VARCHAR2(5)
, CONSTRAINT pdi_to_long_ck CHECK(
    (to_long_deg IS NOT NULL AND to_long_min IS NOT NULL AND to_long_sec IS NOT NULL AND to_long_dir IS NOT NULL)
    OR
    (to_long_deg IS NULL AND to_long_min IS NULL AND to_long_sec IS NULL AND to_long_dir IS NULL)
  )
, length NUMBER
);

CREATE INDEX pdi_pipeline_detail_fk_idx ON ${datasource.user}.pipeline_detail_idents(pipeline_detail_id);

CREATE TABLE ${datasource.user}.pipeline_detail_ident_data (
  id NUMBER GENERATED AS IDENTITY PRIMARY KEY
, pipeline_detail_ident_id NUMBER NOT NULL
    CONSTRAINT pdid_ident_fk REFERENCES ${datasource.user}.pipeline_detail_idents(id)
, component_parts_desc VARCHAR2(4000)
, external_diameter NUMBER
, internal_diameter NUMBER
, wall_thickness NUMBER
, insulation_coating_type VARCHAR2(4000)
, maop NUMBER
, products_to_be_conveyed VARCHAR2(4000)
);

CREATE INDEX ${datasource.user}.pdid_pl_detail_ident_fk_idx ON ${datasource.user}.pipeline_detail_ident_data(pipeline_detail_ident_id);

CREATE TABLE ${datasource.user}.pipeline_detail_migration_data (
  id NUMBER GENERATED AS IDENTITY PRIMARY KEY
, pipeline_detail_id NUMBER NOT NULL
  CONSTRAINT pdmd_ident_fk REFERENCES ${datasource.user}.pipeline_details(id)
, commissioned_date DATE
, abandoned_date DATE
, file_reference VARCHAR2(4000)
, pipe_material VARCHAR2(4000)
, material_grade VARCHAR2(4000)
, trench_depth NUMBER
, system_identifier VARCHAR2(100)
, psig NUMBER
, notes VARCHAR2(4000)
);

CREATE INDEX ${datasource.user}.pdmd_pl_detail_fk_idx ON ${datasource.user}.pipeline_detail_migration_data(pipeline_detail_id);
