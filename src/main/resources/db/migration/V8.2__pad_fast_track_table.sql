CREATE TABLE ${datasource.user}.pad_fast_track_information (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, application_detail_id NUMBER NOT NULL
, avoid_environmental_disaster INTEGER CHECK(avoid_environmental_disaster IN (0, 1) OR avoid_environmental_disaster IS NULL)
, saving_barrels INTEGER CHECK(saving_barrels IN (0, 1) OR saving_barrels IS NULL)
, project_planning INTEGER CHECK(project_planning IN (0, 1) OR project_planning IS NULL)
, has_other_reason INTEGER CHECK(has_other_reason IN (0, 1) OR has_other_reason IS NULL)
, environmental_disaster_reason VARCHAR2(4000)
, saving_barrels_reason VARCHAR2(4000)
, project_planning_reason VARCHAR2(4000)
, other_reason VARCHAR2(4000)
, CONSTRAINT pad_fast_track_info_pad_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);

CREATE INDEX ${datasource.user}.pad_fast_track_info_pad_idx ON ${datasource.user}.pad_fast_track_information (application_detail_id)
TABLESPACE tbsidx;