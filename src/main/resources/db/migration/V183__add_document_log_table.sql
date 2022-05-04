CREATE TABLE ${datasource.user}.docgen_run_section_data (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, docgen_run_id NUMBER
, template_name VARCHAR2(4000)
, html_content CLOB
, CONSTRAINT drsd_dr_id_fk FOREIGN KEY (docgen_run_id) REFERENCES ${datasource.user}.docgen_runs (id)
);

CREATE INDEX ${datasource.user}.drsd_dr_id_idx ON ${datasource.user}.docgen_run_section_data (docgen_run_id);