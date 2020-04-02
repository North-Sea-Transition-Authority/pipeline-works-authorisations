ALTER TABLE ${datasource.user}.pwa_application_contacts
ADD CONSTRAINT pwa_application_contacts_pk PRIMARY KEY (id);

ALTER TABLE ${datasource.user}.pad_project_information
ADD CONSTRAINT pad_project_information_pk PRIMARY KEY (id);

ALTER TABLE ${datasource.user}.pad_env_and_decom
ADD CONSTRAINT pad_env_and_decom_pk PRIMARY KEY (id);