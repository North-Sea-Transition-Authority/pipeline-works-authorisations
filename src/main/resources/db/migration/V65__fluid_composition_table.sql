CREATE TABLE ${datasource.user}.pad_fluid_composition_info (
   id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , application_detail_id NUMBER NOT NULL
    , chemical_name VARCHAR2(4000) NOT NULL
    , fluid_composition_option VARCHAR2(50)
    , mole_value NUMBER
    , CONSTRAINT pad_fluidci_pad_fk FOREIGN KEY(application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

CREATE INDEX ${datasource.user}.pad_fluidci_pad_fk_idx ON ${datasource.user}.pad_fluid_composition_info(application_detail_id);