CREATE TABLE ${datasource.user}.as_built_notification_groups (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pwa_consent_id NUMBER NOT NULL
, CONSTRAINT abng_consent_id_fk FOREIGN KEY (pwa_consent_id) REFERENCES ${datasource.user}.pwa_consents (id)
, reference VARCHAR2(200) NOT NULL
, created_timestamp TIMESTAMP -- dont need created by person here. will be captured on the status. on creation will be system user anyway.
);

CREATE INDEX ${datasource.user}.abng_consent_id_fk_idx ON ${datasource.user}.as_built_notification_groups(pwa_consent_id);

CREATE TABLE ${datasource.user}.as_built_notif_grp_status_hist (
 id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, as_built_notification_group_id NUMBER NOT NULL
, CONSTRAINT abngsh_abng_id_fk FOREIGN KEY (as_built_notification_group_id) REFERENCES ${datasource.user}.as_built_notification_groups (id)
, status VARCHAR2(200) NOT NULL
, created_by_person_id INTEGER NOT NULL
, created_timestamp TIMESTAMP NOT NULL
, ended_by_person_id INTEGER
, ended_timestamp TIMESTAMP
);

CREATE INDEX ${datasource.user}.abngsh_abng_id_fk_idx ON ${datasource.user}.as_built_notif_grp_status_hist(as_built_notification_group_id);

CREATE TABLE ${datasource.user}.as_built_notif_grp_details (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, as_built_notification_group_id NUMBER NOT NULL
, CONSTRAINT abngd_abng_id_fk FOREIGN KEY (as_built_notification_group_id) REFERENCES ${datasource.user}.as_built_notification_groups (id)
, deadline_date DATE NOT NULL
, created_by_person_id INTEGER NOT NULL
, created_timestamp TIMESTAMP NOT NULL
, ended_by_person_id INTEGER
, ended_timestamp TIMESTAMP
);

CREATE INDEX ${datasource.user}.abngd_abng_id_fk_idx ON ${datasource.user}.as_built_notif_grp_details(as_built_notification_group_id);

CREATE TABLE ${datasource.user}.as_built_notif_grp_pipelines (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, as_built_notification_group_id NUMBER NOT NULL
, CONSTRAINT abngp_abng_id_fk FOREIGN KEY (as_built_notification_group_id) REFERENCES ${datasource.user}.as_built_notification_groups (id)
, pipeline_detail_id INTEGER NOT NULL
, CONSTRAINT abngp_pipeline_detail_id_fk FOREIGN KEY (pipeline_detail_id) REFERENCES ${datasource.user}.pipeline_details (id)
, pipeline_change_category VARCHAR2(200) NOT NULL -- [NEW_PIPELINE, CONSENT_UPDATE, OUT_OF_USE, NOT_LAID ]
);

CREATE INDEX ${datasource.user}.abngp_abng_id_fk_idx ON ${datasource.user}.as_built_notif_grp_pipelines(as_built_notification_group_id);

CREATE INDEX ${datasource.user}.abngp_pipeline_det_id_fk_idx ON ${datasource.user}.as_built_notif_grp_pipelines(pipeline_detail_id);