CREATE TABLE ${datasource.user}.pad_pipeline_other_properties (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , application_detail_id NUMBER NOT NULL
    , property_name VARCHAR2(50)
    , availability_option VARCHAR2(50)
    , min_value NUMBER
    , max_value NUMBER
    , CONSTRAINT pad_pipelop_pad_fk FOREIGN KEY(application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

ALTER TABLE ${datasource.user}.pwa_application_details ADD (
    pipeline_phase_properties VARCHAR2(4000),
    other_phase_description VARCHAR2(4000)
);

CREATE INDEX ${datasource.user}.pad_pipelop_pad_fk_idx ON ${datasource.user}.pad_pipeline_other_properties(application_detail_id);