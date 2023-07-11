ALTER TABLE ${datasource.user}.terms_and_conditions_variations ADD PWA_REFERENCE VARCHAR2(20);
ALTER TABLE ${datasource.user}.terms_and_conditions_variations DROP COLUMN PWA_ID;
