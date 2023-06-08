ALTER TABLE ${datasource.user}.pad_location_details ADD transports_materials_from_shore NUMBER;
ALTER TABLE ${datasource.user}.pad_location_details ADD transportation_method_from_shore VARCHAR2(4000);
ALTER TABLE ${datasource.user}.pad_location_details RENAME COLUMN transportation_method TO transportation_method_to_shore;
