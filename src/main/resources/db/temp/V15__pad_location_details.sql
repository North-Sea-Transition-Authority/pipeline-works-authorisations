CREATE TABLE ${datasource.user}.pad_location_details (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, application_detail_id NUMBER NOT NULL
, approximate_location VARCHAR2(4000)
, within_safety_zone VARCHAR2(4000)
, facilities_offshore INTEGER CHECK(facilities_offshore IN (0, 1) OR facilities_offshore IS NULL)
, transports_materials_to_shore INTEGER CHECK(transports_materials_to_shore IN (0, 1) OR transports_materials_to_shore IS NULL)
, transportation_method VARCHAR2(4000)
, CONSTRAINT pad_location_details_pad_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);
CREATE INDEX ${datasource.user}.pad_location_details_pad_idx ON ${datasource.user}.pad_location_details (application_detail_id);

CREATE TABLE ${datasource.user}.pad_safety_zone_structures (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, application_detail_id NUMBER NOT NULL
, facility_id NUMBER
, facility_name_manual_entry VARCHAR2(4000)
, CONSTRAINT pad_safety_zone_structs_pad_fk FOREIGN KEY (application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
, CONSTRAINT pad_safety_zone_structs_check CHECK (
    (facility_name_manual_entry IS NULL AND facility_id IS NOT NULL)
    OR (facility_id IS NULL AND facility_name_manual_entry IS NOT NULL)
    OR (facility_id IS NULL AND facility_name_manual_entry IS NULL)
  )
);
CREATE INDEX ${datasource.user}.pad_safety_zone_struct_pad_idx ON ${datasource.user}.pad_safety_zone_structures (application_detail_id);