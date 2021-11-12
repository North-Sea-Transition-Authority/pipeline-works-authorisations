DECLARE

  PROCEDURE add_new_group(
    p_name VARCHAR2
  , p_abbr VARCHAR2 DEFAULT NULL
  ) IS

BEGIN

  INSERT INTO ${datasource.user}.consultee_groups VALUES (DEFAULT);

  INSERT INTO ${datasource.user}.consultee_group_details (
    cg_id,
    name,
    abbreviation,
    tip_flag,
    version_no,
    start_timestamp,
    csv_response_option_group_list,
    response_document_type) VALUES (
    (SELECT MAX(id) FROM ${datasource.user}.consultee_groups),
    p_name,
    p_abbr,
    1,
    1,
    SYSTIMESTAMP,
    'CONTENT',
    'DEFAULT'
  );

END add_new_group;

BEGIN

  add_new_group(p_name => 'Crown Estate Scotland');

END;
/

UPDATE ${datasource.user}.consultee_group_details
SET display_order = 7
WHERE display_order = 6
AND name = 'BT';

UPDATE ${datasource.user}.consultee_group_details
SET display_order = 6
WHERE name = 'Crown Estate Scotland';