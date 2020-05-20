CREATE TABLE ${datasource.user}.pad_technical_drawings (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pf_id NUMBER
, pad_id NUMBER
, reference VARCHAR2(4000)
, CONSTRAINT pad_tdl_pf_fk FOREIGN KEY(pf_id) REFERENCES ${datasource.user}.pad_files(id)
, CONSTRAINT pad_tdl_pad_fk FOREIGN KEY(pad_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

CREATE INDEX ${datasource.user}.ptd_pf_idx ON ${datasource.user}.pad_technical_drawings (pf_id);
CREATE INDEX ${datasource.user}.ptd_pad_idx ON ${datasource.user}.pad_technical_drawings (pad_id);

CREATE TABLE ${datasource.user}.pad_technical_drawing_links (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, ptd_id NUMBER
, pp_id NUMBER
, CONSTRAINT pad_td_ptdl_fk FOREIGN KEY(ptd_id) REFERENCES ${datasource.user}.pad_technical_drawings(id)
, CONSTRAINT pad_tdl_pp_fk FOREIGN KEY(pp_id) REFERENCES ${datasource.user}.pad_pipelines(id)
);

CREATE INDEX ${datasource.user}.ptdl_ptd_idx ON ${datasource.user}.pad_technical_drawing_links (ptd_id);
CREATE INDEX ${datasource.user}.ptdl_pp_idx ON ${datasource.user}.pad_technical_drawing_links (pp_id);