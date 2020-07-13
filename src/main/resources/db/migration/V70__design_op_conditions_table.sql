CREATE TABLE ${datasource.user}.pad_design_op_conditions (
   id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , application_detail_id NUMBER NOT NULL
    , temperature_op_min NUMBER
    , temperature_op_max NUMBER
    , temperature_design_min NUMBER
    , temperature_design_max NUMBER
    , pressure_op_intl NUMBER
    , pressure_op_extl NUMBER
    , pressure_design_intl NUMBER
    , pressure_design_extl NUMBER
    , flowrate_op_min NUMBER
    , flowrate_op_max NUMBER
    , flowrate_design_min NUMBER
    , flowrate_design_max NUMBER
    , uvalue_op NUMBER
    , uvalue_design NUMBER
    , CONSTRAINT pad_designoc_pad_fk FOREIGN KEY(application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

CREATE INDEX ${datasource.user}.pad_designoc_pad_fk_idx ON ${datasource.user}.pad_design_op_conditions(application_detail_id);
