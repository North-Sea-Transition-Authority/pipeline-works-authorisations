CREATE TABLE ${datasource.user}.mail_merge_fields(
  mnem VARCHAR2(100) NOT NULL PRIMARY KEY
, type VARCHAR2(10) NOT NULL
, text VARCHAR2(4000)
);

CREATE TABLE ${datasource.user}.mail_merge_field_doc_specs(
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, mail_merge_field_mnem VARCHAR2(100) NOT NULL
, doc_spec_mnem VARCHAR2(100) NOT NULL
, CONSTRAINT mail_merge_field_mnem_fk FOREIGN KEY (mail_merge_field_mnem) REFERENCES ${datasource.user}.mail_merge_fields (mnem)
);

CREATE INDEX ${datasource.user}.mail_merge_doc_specs_mnem_idx
ON ${datasource.user}.mail_merge_field_doc_specs (mail_merge_field_mnem);