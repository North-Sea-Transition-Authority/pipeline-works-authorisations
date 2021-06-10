CREATE TABLE ${datasource.user}.as_built_notif_submissions (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , as_built_notif_pipeline_id NUMBER NOT NULL
    , CONSTRAINT abps_abngp_id_fk FOREIGN KEY (as_built_notif_pipeline_id) REFERENCES ${datasource.user}.as_built_notif_grp_pipelines
    , submitted_by_person_id INTEGER NOT NULL
    , submitted_timestamp TIMESTAMP NOT NULL
    , as_built_status VARCHAR2(200) NOT NULL
    , date_laid DATE
    , date_pipeline_brought_into_use DATE
    , regulator_submission_reason VARCHAR2(4000)
);

CREATE INDEX ${datasource.user}.abns_abngp_id_fk_idx ON ${datasource.user}.as_built_notif_submissions(as_built_notif_pipeline_id);