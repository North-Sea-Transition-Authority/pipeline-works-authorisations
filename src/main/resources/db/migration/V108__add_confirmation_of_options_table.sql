CREATE TABLE ${datasource.user}.pad_confirmation_of_option (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, application_detail_id NUMBER NOT NULL
, CONSTRAINT pad_coo_pad_id_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
, confirmation_type VARCHAR2(4000)
, chosen_option_desc VARCHAR2(4000)
);

CREATE INDEX ${datasource.user}.pad_coo_pad_id_fk_idx ON ${datasource.user}.pad_confirmation_of_option(application_detail_id);