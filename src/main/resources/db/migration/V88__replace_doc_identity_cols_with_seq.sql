ALTER TABLE ${datasource.user}.dt_section_clauses MODIFY id DROP IDENTITY;

CREATE SEQUENCE ${datasource.user}.dt_section_clauses_id_seq;

ALTER TABLE ${datasource.user}.dt_section_clause_versions MODIFY id DROP IDENTITY;

CREATE SEQUENCE ${datasource.user}.dt_scv_id_seq;

ALTER TABLE ${datasource.user}.di_section_clauses MODIFY id DROP IDENTITY;

CREATE SEQUENCE ${datasource.user}.di_section_clauses_id_seq
INCREMENT BY 100;

ALTER TABLE ${datasource.user}.di_sc_versions MODIFY id DROP IDENTITY;

CREATE SEQUENCE ${datasource.user}.di_sc_versions_id_seq
INCREMENT BY 100;

ALTER TABLE ${datasource.user}.document_instances ADD created_timestamp TIMESTAMP;

UPDATE ${datasource.user}.document_instances
SET created_timestamp = SYSTIMESTAMP;

ALTER TABLE ${datasource.user}.document_instances MODIFY created_timestamp NOT NULL;