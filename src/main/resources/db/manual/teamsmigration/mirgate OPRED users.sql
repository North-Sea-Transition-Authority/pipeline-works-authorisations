DECLARE

  l_uuid VARCHAR2(36);
  l_team_id VARCHAR2(36);

  FUNCTION new_uuid RETURN VARCHAR2 IS
  BEGIN
    -- Creates a numeric only uuid in standard format XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
  RETURN
    LPAD(ROUND(dbms_random.value(0,99999999)), 8, '0') || '-' ||
    LPAD(ROUND(dbms_random.value(0,9999)), 4, '0') || '-' ||
    LPAD(ROUND(dbms_random.value(0,9999)), 4, '0') || '-' ||
    LPAD(ROUND(dbms_random.value(0,9999)), 4, '0') || '-' ||
    LPAD(ROUND(dbms_random.value(0,999999999999)), 12, '0');
  END;

BEGIN

  SELECT t.id
  INTO l_team_id
  FROM pwa.teams t
  WHERE t.name = 'OPRED'
  AND t.type = 'SECONDARY_REGULATOR';


  FOR team_roles IN (
    SELECT rmc.wua_id, 'CONSENT_VIEWER' role
    FROM decmgr.resource_members_current rmc
    JOIN securemgr.web_user_accounts wua ON wua.id = rmc.wua_id
    WHERE rmc.res_type = 'SECTION29_DTI_SUPER_USERS'
    AND rmc.role_name IN ('ODU_PIPELINE_VIEW', 'ODU_PIPELINE_EDIT')
    AND wua.account_status = 'ACTIVE'
    UNION
    SELECT rmc.wua_id, 'TEAM_ADMINISTRATOR' role
    FROM decmgr.resource_members_current rmc
    JOIN securemgr.web_user_accounts wua ON wua.id = rmc.wua_id
    WHERE rmc.res_type = 'SECTION29_DTI_SUPER_USERS'
    AND rmc.role_name ='RESOURCE_COORDINATOR'
    AND wua.account_status = 'ACTIVE'
  )
  LOOP

    l_uuid := new_uuid();


    INSERT INTO pwa.team_roles(id, team_id, role, wua_id)
    VALUES(l_uuid, l_team_id, team_roles.role, team_roles.wua_id);


  END LOOP;

  COMMIT;

END;
/



