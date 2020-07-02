DECLARE

  l_cg_id NUMBER;

BEGIN

  SELECT cgd.cg_id
  INTO l_cg_id
  FROM ${datasource.user}.consultee_group_details cgd
  WHERE cgd.name = 'Denmark treaty consultees';

  DELETE FROM ${datasource.user}.consultee_group_team_members
  WHERE cg_id = l_cg_id;

  DELETE FROM ${datasource.user}.consultee_group_details
  WHERE cg_id = l_cg_id;

  DELETE FROM ${datasource.user}.consultee_groups
  WHERE id = l_cg_id;

END;
/