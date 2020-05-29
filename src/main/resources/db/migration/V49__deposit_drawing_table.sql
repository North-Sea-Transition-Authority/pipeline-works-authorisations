CREATE TABLE ${datasource.user}.pad_deposit_drawings (
   id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , pf_id NUMBER
    , pad_id NUMBER
    , reference VARCHAR2(4000)
    , CONSTRAINT pad_depd_pf_fk FOREIGN KEY(pf_id) REFERENCES ${datasource.user}.pad_files(id)
    , CONSTRAINT pad_depd_pad_fk FOREIGN KEY(pad_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

CREATE INDEX ${datasource.user}.depd_pf_idx ON ${datasource.user}.pad_deposit_drawings (pf_id);
CREATE INDEX ${datasource.user}.depd_pad_idx ON ${datasource.user}.pad_deposit_drawings (pad_id);