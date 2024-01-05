UPDATE ${datasource.user}.pwa_application_details pad SET pad.IS_LINKED_TO_AREA = pad.IS_LINKED_TO_FIELD WHERE pad.IS_LINKED_TO_FIELD IS NULL;
ALTER TABLE ${datasource.user}.pwa_application_details DROP COLUMN IS_LINKED_TO_FIELD;
