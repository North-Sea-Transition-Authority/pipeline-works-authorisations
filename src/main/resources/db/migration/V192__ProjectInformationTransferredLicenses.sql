CREATE TABLE ${datasource.user}.PAD_PROJECT_INFORMATION_LICENCE_APPLICATIONS (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , PAD_PROJECT_INFORMATION_ID NUMBER NOT NULL
    , PEARS_LICENCE_APPLICATION_NUMBER NUMBER NOT NULL
    , created_timestamp TIMESTAMP NOT NULL
    , CONSTRAINT padpil_padpi_fk FOREIGN KEY (PAD_PROJECT_INFORMATION_ID) REFERENCES ${datasource.user}.PAD_PROJECT_INFORMATION (id)
);
CREATE INDEX ${datasource.user}.pears_applications_project_info_fk_idx ON ${datasource.user}.pad_project_information_licence_applications(pad_project_information_id);
