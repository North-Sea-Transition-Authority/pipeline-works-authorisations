ALTER TABLE ${datasource.user}.consultee_group_details
ADD csv_response_option_group_list VARCHAR2(4000);

UPDATE ${datasource.user}.consultee_group_details
SET csv_response_option_group_list = 'CONTENT';

UPDATE ${datasource.user}.consultee_group_details
SET csv_response_option_group_list = 'ADVICE'
WHERE abbreviation = 'HSE';

ALTER TABLE ${datasource.user}.consultee_group_details
MODIFY csv_response_option_group_list VARCHAR2(4000) NOT NULL;