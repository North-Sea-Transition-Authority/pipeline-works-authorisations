CREATE TABLE ${datasource.user}.pad_campaign_work_schedule (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pad_id NUMBER NOT NULL CONSTRAINT pcw_pad_fk REFERENCES ${datasource.user}.pwa_application_details(id)
, work_from_date DATE NOT NULL
, work_to_date DATE NOT NULL
);

CREATE INDEX ${datasource.user}.pcw_ppad_fk_idx ON ${datasource.user}.pad_campaign_work_schedule (pad_id);

CREATE TABLE ${datasource.user}.pad_campaign_works_pipelines (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pad_campaign_work_schedule_id NUMBER NOT NULL CONSTRAINT pcwp_fk REFERENCES ${datasource.user}.pad_campaign_work_schedule(id)
, pad_pipeline_id NUMBER NOT NULL CONSTRAINT pcwp_pipeline_fk REFERENCES ${datasource.user}.pad_pipelines(id)
);

CREATE INDEX ${datasource.user}.pcwp_pcws_fk_idx ON ${datasource.user}.pad_campaign_works_pipelines (pad_campaign_work_schedule_id);
CREATE INDEX ${datasource.user}.pcwp_pipeline_fk_idx ON ${datasource.user}.pad_campaign_works_pipelines (pad_pipeline_id);