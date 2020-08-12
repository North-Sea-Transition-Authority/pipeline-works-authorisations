ALTER TABLE ${datasource.user}.pad_pipelines
ADD pipeline_status VARCHAR2(4000) DEFAULT 'IN_SERVICE' NOT NULL
ADD pipeline_status_reason VARCHAR2(4000);

ALTER TABLE ${datasource.user}.pipeline_details
ADD pipeline_status_reason VARCHAR2(4000);