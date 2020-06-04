CREATE TABLE ${datasource.user}.pad_deposit_drawings (
   id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , pf_id NUMBER NOT NULL
    , pad_id NUMBER NOT NULL
    , reference VARCHAR2(4000) NOT NULL
    , CONSTRAINT pad_depd_pf_fk FOREIGN KEY(pf_id) REFERENCES ${datasource.user}.pad_files(id)
    , CONSTRAINT pad_depd_pad_fk FOREIGN KEY(pad_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

CREATE INDEX ${datasource.user}.depd_pf_idx ON ${datasource.user}.pad_deposit_drawings (pf_id);
CREATE INDEX ${datasource.user}.depd_pad_idx ON ${datasource.user}.pad_deposit_drawings (pad_id);

CREATE TABLE ${datasource.user}.pad_deposit_drawing_links (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , pad_permanent_deposit_id NUMBER NOT NULL
    , pad_deposit_drawing_id NUMBER NOT NULL
    , CONSTRAINT pad_depdlink_permdep_fk FOREIGN KEY(pad_permanent_deposit_id) REFERENCES ${datasource.user}.pad_permanent_deposits(id)
    , CONSTRAINT pad_depdlink_depdid_fk FOREIGN KEY(pad_deposit_drawing_id) REFERENCES ${datasource.user}.pad_deposit_drawings(id)
);

CREATE INDEX ${datasource.user}.depdlink_permdep_idx ON ${datasource.user}.pad_deposit_drawing_links (pad_permanent_deposit_id);
CREATE INDEX ${datasource.user}.depdlink_depdid_idx ON ${datasource.user}.pad_deposit_drawing_links (pad_deposit_drawing_id);
