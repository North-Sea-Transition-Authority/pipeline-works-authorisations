CREATE TABLE ${datasource.user}.app_files(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pa_id NUMBER NOT NULL
, file_id VARCHAR2(4000) NOT NULL
, description VARCHAR2(4000)
, purpose VARCHAR2(100) NOT NULL
, file_link_status VARCHAR2(100) NOT NULL
, CONSTRAINT af_pa_id_fk FOREIGN KEY (pa_id) REFERENCES ${datasource.user}.pwa_applications (id)
, CONSTRAINT af_file_id_fk FOREIGN KEY (file_id) REFERENCES ${datasource.user}.uploaded_files (file_id)
);

CREATE INDEX ${datasource.user}.af_pa_idx ON ${datasource.user}.app_files (pa_id);
CREATE INDEX ${datasource.user}.af_uf_idx ON ${datasource.user}.app_files (file_id);

ALTER TABLE ${datasource.user}.case_notes MODIFY id PRIMARY KEY;

CREATE TABLE ${datasource.user}.case_note_document_links (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  case_note_id NUMBER NOT NULL,
  af_id NUMBER NOT NULL,
  CONSTRAINT cndl_cn_id_fk FOREIGN KEY (case_note_id) REFERENCES ${datasource.user}.case_notes (id),
  CONSTRAINT cndl_af_id_fk FOREIGN KEY (af_id) REFERENCES ${datasource.user}.app_files (id)
);

CREATE INDEX ${datasource.user}.case_note_doc_link_cn_id_idx ON ${datasource.user}.case_note_document_links (case_note_id);

CREATE INDEX ${datasource.user}.case_note_doc_link_af_id_idx ON ${datasource.user}.case_note_document_links (af_id);