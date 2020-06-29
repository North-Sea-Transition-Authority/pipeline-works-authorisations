ALTER TABLE ${datasource.user}.consultee_group_details MODIFY abbreviation NULL;

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
      start_timestamp) VALUES (
      (SELECT MAX(id) FROM ${datasource.user}.consultee_groups),
      p_name,
      p_abbr,
      1,
      1,
      SYSTIMESTAMP
    );

  END add_new_group;

BEGIN

  add_new_group(
    p_name => 'Offshore Decommissioning Unit'
  , p_abbr => 'ODU'
  );

  add_new_group(
      p_name => 'Health and Safety Executive'
    , p_abbr => 'HSE'
  );

  add_new_group(p_name => 'Crown Estate');

  add_new_group(p_name => 'BT');

  add_new_group(p_name => 'OGA Technical Team');

  add_new_group(p_name => 'Norway treaty consultees');

  add_new_group(p_name => 'Ireland treaty consultees');

  add_new_group(p_name => 'Denmark treaty consultees');

  add_new_group(p_name => 'Netherlands treaty consultees');

  add_new_group(p_name => 'Belgium treaty consultees');

END;
/