CREATE TABLE ${datasource.user}.document_templates (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, mnem VARCHAR2(4000) NOT NULL
);

CREATE TABLE ${datasource.user}.dt_sections (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, dt_id NUMBER NOT NULL
, name VARCHAR2(4000) NOT NULL
, status VARCHAR2(4000) NOT NULL
, start_timestamp TIMESTAMP NOT NULL
, end_timestamp TIMESTAMP
, CONSTRAINT ds_dt_id_fk FOREIGN KEY (dt_id) REFERENCES ${datasource.user}.document_templates (id)
);

CREATE INDEX ${datasource.user}.dt_sections_dt_id_idx ON ${datasource.user}.dt_sections (dt_id);

CREATE TABLE ${datasource.user}.dt_section_clauses (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, s_id NUMBER NOT NULL
, CONSTRAINT dsc_s_id_fk FOREIGN KEY (s_id) REFERENCES ${datasource.user}.dt_sections (id)
);

CREATE INDEX ${datasource.user}.dt_section_clauses_s_id_idx ON ${datasource.user}.dt_section_clauses (s_id);

CREATE TABLE ${datasource.user}.dt_section_clause_versions (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, sc_id NUMBER NOT NULL
, version_no NUMBER NOT NULL
, tip_flag INTEGER
, name VARCHAR2(4000) NOT NULL
, text CLOB
, parent_sc_id NUMBER
, level_order NUMBER
, status VARCHAR2(4000) NOT NULL
, created_timestamp TIMESTAMP NOT NULL
, created_by_person_id NUMBER NOT NULL
, ended_timestamp TIMESTAMP
, ended_by_person_id NUMBER
, CONSTRAINT dscv_sc_id_fk FOREIGN KEY (sc_id) REFERENCES ${datasource.user}.dt_section_clauses (id)
, CONSTRAINT dscv_parent_sc_id_fk FOREIGN KEY (parent_sc_id) REFERENCES ${datasource.user}.dt_section_clauses (id)
, CONSTRAINT dscv_tip_flag_ck CHECK(tip_flag IN (0, 1) OR tip_flag IS NULL)
);

CREATE INDEX ${datasource.user}.dt_scv_sc_id_idx ON ${datasource.user}.dt_section_clause_versions (sc_id);

CREATE INDEX ${datasource.user}.dt_scv_parent_sc_id_idx ON ${datasource.user}.dt_section_clause_versions (parent_sc_id);

CREATE TABLE ${datasource.user}.document_instances (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, dt_id NUMBER NOT NULL
, pwa_application_id NUMBER NOT NULL
, CONSTRAINT di_dt_id_fk FOREIGN KEY (dt_id) REFERENCES ${datasource.user}.document_templates (id)
, CONSTRAINT di_pwa_application_id_fk FOREIGN KEY (pwa_application_id) REFERENCES ${datasource.user}.pwa_applications (id)
);

CREATE INDEX ${datasource.user}.document_instances_dt_id_idx ON ${datasource.user}.document_instances (dt_id);

CREATE INDEX ${datasource.user}.document_instances_pa_id_idx ON ${datasource.user}.document_instances (pwa_application_id);

CREATE TABLE ${datasource.user}.di_section_clauses (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, di_id NUMBER NOT NULL
, dt_sc_id NUMBER NOT NULL
, CONSTRAINT disc_di_id_fk FOREIGN KEY (di_id) REFERENCES ${datasource.user}.document_instances (id)
, CONSTRAINT disc_sc_id_fk FOREIGN KEY (dt_sc_id) REFERENCES ${datasource.user}.dt_section_clauses (id)
);

CREATE INDEX ${datasource.user}.di_sc_di_id_idx ON ${datasource.user}.di_section_clauses (di_id);

CREATE INDEX ${datasource.user}.di_sc_dt_sc_id_idx ON ${datasource.user}.di_section_clauses (dt_sc_id);

CREATE TABLE ${datasource.user}.di_sc_versions (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, di_sc_id NUMBER NOT NULL
, version_no NUMBER NOT NULL
, tip_flag INTEGER
, name VARCHAR2(4000) NOT NULL
, text CLOB
, parent_di_sc_id NUMBER
, level_order NUMBER
, status VARCHAR2(4000)
, created_timestamp TIMESTAMP NOT NULL
, created_by_person_id NUMBER NOT NULL
, ended_timestamp TIMESTAMP
, ended_by_person_id NUMBER
, CONSTRAINT dsv_di_sc_id FOREIGN KEY (di_sc_id) REFERENCES ${datasource.user}.di_section_clauses (id)
, CONSTRAINT dsv_parent_di_sc_id FOREIGN KEY (parent_di_sc_id) REFERENCES ${datasource.user}.di_section_clauses (id)
);

CREATE INDEX ${datasource.user}.di_sc_versions_di_sc_id_idx ON ${datasource.user}.di_sc_versions (di_sc_id);

CREATE INDEX ${datasource.user}.di_sc_versions_pa_di_sc_id_idx ON ${datasource.user}.di_sc_versions (parent_di_sc_id);