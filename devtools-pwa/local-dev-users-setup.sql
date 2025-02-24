DECLARE
  reg_team_id VARCHAR2(4000) := '683daec8-0319-4156-9806-eaca16f55c7f';

  shell_org_team_id VARCHAR2(4000) := '3fe3a69a-1bbe-42d7-93ca-5689b42cbe9b';
  bp_org_team_id VARCHAR2(4000) := 'a7689eae-e01c-4ac8-b3ef-06935e81cd7b';

  nsta_team_id VARCHAR2(4000) := '25d766cc-e2d1-46a8-8390-37ac6561a8c4';
  emt_team_id VARCHAR2(4000) := '25d766cc-e2d1-46a8-8390-37ac6561a8c6';
BEGIN

  -- Reg team
  INSERT INTO pwa_xx.teams(id, type, name)
  VALUES (reg_team_id, 'REGULATOR', 'PWA Regulator Team');

  -- administrator@pwa.co.uk
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('a0af1f10-6341-4af6-a8a7-32ed60b5dc00', reg_team_id, 'TEAM_ADMINISTRATOR', 40272);
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('a0af1f10-6341-4af6-a8a7-32ed60b5dc01', reg_team_id, 'ORGANISATION_MANAGER', 40272);

  -- case.officer@pwa.co.uk
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('a0af1f10-6341-4af6-a8a7-32ed60b5dc10', reg_team_id, 'CASE_OFFICER', 52912);

  -- consent.viewer@pwa.co.uk
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('a0af1f10-6341-4af6-a8a7-32ed60b5dc20', reg_team_id, 'CONSENT_VIEWER', 52572);

  -- asbuilt.admin@pwa.co.uk
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('a0af1f10-6341-4af6-a8a7-32ed60b5dc30', reg_team_id, 'AS_BUILT_NOTIFICATION_ADMIN', 52735);


  -- SHELL org team
  INSERT INTO pwa_xx.teams(id, type, name, scope_type, scope_id)
  VALUES (shell_org_team_id, 'ORGANISATION', 'ROYAL DUTCH SHELL', 'ORGGRP', '116');

  -- administrator.shell@pwa.co.uk
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('6258755c-461e-4f32-89b1-40b62f488a01', shell_org_team_id, 'TEAM_ADMINISTRATOR', 40273);
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('6258755c-461e-4f32-89b1-40b62f488a02', shell_org_team_id, 'ORGANISATION_MANAGER', 40273);

  -- finance.shell@pwa.co.uk
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('6258755c-461e-4f32-89b1-40b62f488a04', shell_org_team_id, 'FINANCE_ADMIN', 52692);

  -- submitter2.shell@pwa.co.uk
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('6258755c-461e-4f32-89b1-40b62f488a05', shell_org_team_id, 'APPLICATION_CREATOR', 52594);
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('6258755c-461e-4f32-89b1-40b62f488a06', shell_org_team_id, 'APPLICATION_SUBMITTER', 52594);


  -- BP org team
  INSERT INTO pwa_xx.teams(id, type, name, scope_type, scope_id)
  VALUES (bp_org_team_id, 'ORGANISATION', 'BP EXPLORATION', 'ORGGRP', '50');

  -- administrator.bp@pwa.co.uk
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('6258755c-461e-4f32-89b1-40b62f488a11', bp_org_team_id, 'TEAM_ADMINISTRATOR', 40412);
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('6258755c-461e-4f32-89b1-40b62f488a12', bp_org_team_id, 'APPLICATION_CREATOR', 40412);
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('6258755c-461e-4f32-89b1-40b62f488a13', bp_org_team_id, 'APPLICATION_SUBMITTER', 40412);

  -- finance.rwedea@pwa.co.uk
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('6258755c-461e-4f32-89b1-40b62f488a14', bp_org_team_id, 'FINANCE_ADMIN', 52713);
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('6258755c-461e-4f32-89b1-40b62f488a15', bp_org_team_id, 'AS_BUILT_NOTIFICATION_SUBMITTER', 52713);


  -- NSTA Technical Team
  INSERT INTO pwa_xx.teams(id, type, name)
  VALUES (nsta_team_id, 'CONSULTEE', 'NSTA Technical Team', 'CONSULTEE', 'NSTA');

  -- oga.tech.team@pwa.co.uk
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('156b4dd6-c7f3-4f37-9d8a-2f3279c2c401', nsta_team_id, 'TEAM_ADMINISTRATOR', 53492);
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('156b4dd6-c7f3-4f37-9d8a-2f3279c2c402', nsta_team_id, 'RESPONDER', 53492);
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('156b4dd6-c7f3-4f37-9d8a-2f3279c2c403', nsta_team_id, 'RECIPIENT', 53492);


  -- Environmental Management Team
  INSERT INTO pwa_xx.teams(id, type, name)
  VALUES (emt_team_id, 'CONSULTEE', 'Environmental Management Team', 'CONSULTEE', 'EMT');

  -- emt.consultee@emt.co.uk
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('186b4dd6-c7f3-4f37-9d8a-2f3279c2c401', emt_team_id, 'TEAM_ADMINISTRATOR', 52772);
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('186b4dd6-c7f3-4f37-9d8a-2f3279c2c402', emt_team_id, 'RESPONDER', 52772);
  INSERT INTO pwa_xx.team_roles(id, team_id, role, wua_id)
  VALUES ('186b4dd6-c7f3-4f37-9d8a-2f3279c2c403', emt_team_id, 'RECIPIENT', 52772);

END;