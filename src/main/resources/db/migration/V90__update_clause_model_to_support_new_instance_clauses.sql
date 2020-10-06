ALTER TABLE ${datasource.user}.di_section_clauses MODIFY dt_sc_id NULL;

ALTER TABLE ${datasource.user}.di_section_clauses ADD (
  dt_s_id NUMBER,
  CONSTRAINT dt_s_id_fk FOREIGN KEY (dt_s_id) REFERENCES ${datasource.user}.dt_sections (id),
  CONSTRAINT dt_ck CHECK (
      (dt_sc_id IS NOT NULL AND dt_s_id IS NULL) OR
      (dt_sc_id IS NULL AND dt_s_id IS NOT NULL)
    )
  );

CREATE INDEX ${datasource.user}.dsc_dt_s_id_idx ON ${datasource.user}.di_section_clauses (dt_s_id);