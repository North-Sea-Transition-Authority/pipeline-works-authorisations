CREATE TABLE ${datasource.user}.pad_pipeline_tech_info (
   id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , application_detail_id NUMBER NOT NULL
    , estimated_field_life NUMBER
    , pipeline_designed_to_standards INTEGER CHECK(pipeline_designed_to_standards IN (0, 1) OR pipeline_designed_to_standards IS NULL)
    , pipeline_standards_description VARCHAR2(4000)
    , corrosion_description VARCHAR2(4000)
    , planned_pipeline_tie_in_points INTEGER CHECK(planned_pipeline_tie_in_points IN (0, 1) OR planned_pipeline_tie_in_points IS NULL)
    , tie_in_points_description VARCHAR2(4000)
    , CONSTRAINT pad_pipeti_pad_fk FOREIGN KEY(application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);