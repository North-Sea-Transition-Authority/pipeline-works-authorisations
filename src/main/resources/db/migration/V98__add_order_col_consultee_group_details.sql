ALTER TABLE ${datasource.user}.consultee_group_details
    ADD display_order NUMBER;

UPDATE ${datasource.user}.consultee_group_details
SET display_order = 1
WHERE name = 'OGA Technical Team';

UPDATE ${datasource.user}.consultee_group_details
SET display_order = 2
WHERE name = 'Environmental Management Team';

UPDATE ${datasource.user}.consultee_group_details
SET display_order = 3
WHERE name = 'Offshore Decommissioning Unit';

UPDATE ${datasource.user}.consultee_group_details
SET display_order = 4
WHERE name = 'Health and Safety Executive';

UPDATE ${datasource.user}.consultee_group_details
SET display_order = 5
WHERE name = 'Crown Estate';

UPDATE ${datasource.user}.consultee_group_details
SET display_order = 6
WHERE name = 'BT';