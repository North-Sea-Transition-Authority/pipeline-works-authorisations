CREATE TABLE ${datasource.user}.consultee_groups (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
);

CREATE TABLE ${datasource.user}.consultee_group_details (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  cg_id NUMBER NOT NULL,
  name VARCHAR2(4000) NOT NULL,
  abbreviation VARCHAR2(4000) NOT NULL,
  tip_flag NUMBER,
  version_no NUMBER NOT NULL,
  start_timestamp TIMESTAMP NOT NULL,
  end_timestamp TIMESTAMP,
  CONSTRAINT cgd_cg_id_fk FOREIGN KEY (cg_id) REFERENCES ${datasource.user}.consultee_groups (id),
  CONSTRAINT cgd_tip_flag_ck CHECK(tip_flag IN (0, 1) OR tip_flag IS NULL),
  CONSTRAINT cgd_ts_ck CHECK ((tip_flag = 1 AND end_timestamp IS NULL) OR (tip_flag IS NULL AND end_timestamp IS NOT NULL))
);

CREATE INDEX ${datasource.user}.cgd_cg_id_idx ON ${datasource.user}.consultee_group_details (cg_id);

CREATE TABLE ${datasource.user}.consultee_group_team_members (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  cg_id NUMBER NOT NULL,
  person_id NUMBER NOT NULL,
  csv_role_list VARCHAR2(4000),
  CONSTRAINT cgt_cg_id_fk FOREIGN KEY (cg_id) REFERENCES ${datasource.user}.consultee_groups (id)
);

CREATE INDEX ${datasource.user}.cgt_cg_id_idx ON ${datasource.user}.consultee_group_team_members (cg_id);

INSERT INTO ${datasource.user}.consultee_groups VALUES (DEFAULT);

INSERT INTO ${datasource.user}.consultee_group_details (
  cg_id,
  name,
  abbreviation,
  tip_flag,
  version_no,
  start_timestamp) VALUES (
  (SELECT MAX(id) FROM ${datasource.user}.consultee_groups),
  'Environmental Management Team',
  'EMT',
  1,
  1,
  SYSTIMESTAMP
);
