ALTER TABLE ${datasource.user}.pad_fields RENAME TO pad_linked_areas;
ALTER TABLE ${datasource.user}.pad_linked_areas RENAME COLUMN field_name_manual_entry TO area_name_manual_entry;
ALTER TABLE ${datasource.user}.pad_linked_areas ADD area_type VARCHAR2(50);
