CREATE TABLE ${datasource.user}.pad_crossed_storage_areas (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , pad_id NUMBER NOT NULL
    , storage_area_ref VARCHAR2(4000)
    , location VARCHAR2(4000)
    , crossing_owner VARCHAR2(4000)
    , created_timestamp TIMESTAMP
    , CONSTRAINT pad_crossed_storage_areas_pad_fk FOREIGN KEY (pad_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);
CREATE INDEX ${datasource.user}.pad_storage_areas_pad_idx ON ${datasource.user}.pad_crossed_storage_areas (pad_id);

CREATE TABLE ${datasource.user}.pad_storage_area_crossing_owners (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , pad_storage_area_crossing_id NUMBER NOT NULL
    , owner_ou_id NUMBER
    , owner_name VARCHAR2(4000)
    , CONSTRAINT pad_sta_owner_ck CHECK ((owner_ou_id IS NULL AND owner_name IS NOT NULL) OR (owner_ou_id IS NOT NULL AND owner_name IS NULL))
    , CONSTRAINT pad_stac_fk FOREIGN KEY (pad_storage_area_crossing_id) REFERENCES ${datasource.user}.pad_crossed_storage_areas(id)
);
CREATE INDEX ${datasource.user}.pad_staco_fk_idx ON ${datasource.user}.pad_storage_area_crossing_owners (pad_storage_area_crossing_id);
