ALTER TABLE ${datasource.user}.pwa_application_details ADD supplementary_documents_flag NUMBER;

ALTER TABLE ${datasource.user}.pwa_application_details
ADD CONSTRAINT pad_supp_doc_flag_ck CHECK (supplementary_documents_flag IN (0,1) OR supplementary_documents_flag IS NULL);