ALTER TABLE ${datasource.user}.pad_files
  DROP CONSTRAINT pf_file_id_fk;

ALTER TABLE ${datasource.user}.app_files
DROP CONSTRAINT af_file_id_fk;