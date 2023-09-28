ALTER TABLE ${datasource.user}.terms_and_conditions_variations RENAME TO terms_and_conditions;

ALTER TABLE ${datasource.user}.terms_and_conditions ADD huoo_term_one NUMBER;
ALTER TABLE ${datasource.user}.terms_and_conditions ADD huoo_term_two NUMBER;
ALTER TABLE ${datasource.user}.terms_and_conditions ADD huoo_term_three NUMBER;
ALTER TABLE ${datasource.user}.terms_and_conditions DROP COLUMN huoo_terms;
