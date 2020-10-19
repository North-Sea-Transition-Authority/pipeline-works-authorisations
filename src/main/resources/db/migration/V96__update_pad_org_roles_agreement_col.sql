
UPDATE ${datasource.user}.pad_organisation_roles
SET agreement = 'ANY_TREATY_COUNTRY'
WHERE agreement IS NOT NULL;
