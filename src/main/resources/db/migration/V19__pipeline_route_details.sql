ALTER TABLE ${datasource.user}.pad_location_details
ADD route_survey_undertaken INTEGER CHECK(route_survey_undertaken IN (0,1) OR route_survey_undertaken IS NULL)
ADD survey_concluded_timestamp TIMESTAMP
ADD within_limits_of_deviation INTEGER CHECK(within_limits_of_deviation IN (0,1) OR within_limits_of_deviation IS NULL)
ADD pipeline_route_details CLOB;

CREATE TABLE ${datasource.user}.pad_location_detail_files (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, application_detail_id NUMBER NOT NULL
, file_id VARCHAR2(4000) NOT NULL
, description VARCHAR2(4000)
, file_link_status VARCHAR2(100)
, CONSTRAINT pad_ldf_pad_id_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details (id)
, CONSTRAINT pad_ldf_file_id_fk FOREIGN KEY (file_id) REFERENCES ${datasource.user}.uploaded_files (file_id)
) TABLESPACE tbsdata;

CREATE INDEX ${datasource.user}.pad_ldf_pad_idx ON ${datasource.user}.pad_location_detail_files (application_detail_id);
CREATE INDEX ${datasource.user}.pad_ldf_uf_idx ON ${datasource.user}.pad_location_detail_files (file_id);