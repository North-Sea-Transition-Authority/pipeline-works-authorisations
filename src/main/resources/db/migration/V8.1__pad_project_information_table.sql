CREATE TABLE ${datasource.user}.pad_project_information (
  id NUMBER GENERATED ALWAYS AS IDENTITY
, application_detail_id NUMBER NOT NULL
, project_name VARCHAR2(4000)
, project_overview VARCHAR2(4000)
, project_diagram_file_id NUMBER -- TODO: !!PWA-370!! Foreign key reference to files table.
, method_of_pipeline_deployment VARCHAR2(4000)
, proposed_start_timestamp TIMESTAMP
, mobilisation_timestamp TIMESTAMP
, earliest_completion_timestamp TIMESTAMP
, latest_completion_timestamp TIMESTAMP
, using_campaign_approach INTEGER CHECK(using_campaign_approach IN (0, 1) OR using_campaign_approach IS NULL)
, CONSTRAINT pad_project_information_pad_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

CREATE INDEX ${datasource.user}.pad_project_info_pad_idx ON ${datasource.user}.pad_project_information (application_detail_id)
TABLESPACE tbsidx;