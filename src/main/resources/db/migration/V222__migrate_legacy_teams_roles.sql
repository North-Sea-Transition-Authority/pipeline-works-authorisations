DECLARE
  l_team_id VARCHAR2(4000);
  l_team_role_id VARCHAR2(4000);
  l_team_count NUMBER;
  l_team_role_count NUMBER;
  l_uuid VARCHAR2(36);

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

  FOR team_record IN (
    SELECT
      pr.res_id
    , pr.description name
    , CASE pr.res_type
        WHEN 'PWA_REGULATOR_TEAM' THEN 'REGULATOR'
        WHEN 'PWA_ORGANISATION_TEAM' THEN 'ORGANISATION'
      END type
    , REPLACE(pruc.uref, '++REGORGGRP', '') AS scope_id
    , CASE
        WHEN pruc.uref IS NOT NULL THEN 'ORGGRP'
      END scope_type
    FROM ${datasource.user}.portal_resources pr
    LEFT JOIN ${datasource.user}.portal_resource_usages_current pruc ON pruc.res_id = pr.res_id AND pruc.uref LIKE '%++REGORGGRP'
    WHERE pr.res_type IN ('PWA_REGULATOR_TEAM', 'PWA_ORGANISATION_TEAM')
  ) LOOP
    -- Check if the team already exists to avoid duplicates
    SELECT COUNT(*)
    INTO l_team_count
    FROM ${datasource.user}.teams t
    WHERE t.type = team_record.type
      AND t.scope_type = team_record.scope_type
      AND t.scope_id  = team_record.scope_id;

    IF l_team_count = 0 THEN
      l_uuid := new_uuid();
      -- Insert the team
      INSERT INTO ${datasource.user}.teams (id, type, name, scope_type, scope_id)
      VALUES (l_uuid, team_record.type, team_record.name, team_record.scope_type, team_record.scope_id)
      RETURNING id INTO l_team_id;

      FOR team_role_record IN (
        SELECT
          ua.wua_id
        , CASE prmcr.role_name
            WHEN 'RESOURCE_COORDINATOR'     THEN 'TEAM_ADMINISTRATOR'
            WHEN 'ACCESS_MANAGER'           THEN 'TEAM_ADMINISTRATOR'
            WHEN 'APPLICATION_CREATE'       THEN 'APPLICATION_CREATOR'
            WHEN 'APPLICATION_SUBMITTER'    THEN 'APPLICATION_SUBMITTER'
            WHEN 'FINANCE_ADMIN'            THEN 'FINANCE_ADMIN'
            WHEN 'AS_BUILT_NOTIF_SUBMITTE'  THEN 'AS_BUILT_NOTIFICATION_SUBMITTER'
            WHEN 'ORGANISATION_MANAGER'     THEN 'ORGANISATION_MANAGER'
            WHEN 'PWA_MANAGER'              THEN 'PWA_MANAGER'
            WHEN 'CASE_OFFICER'             THEN 'CASE_OFFICER'
            WHEN 'PWA_CONSENT_VIEWER'       THEN 'CONSENT_VIEWER'
            WHEN 'AS_BUILT_NOTIF_ADMIN'     THEN 'AS_BUILT_NOTIFICATION_ADMIN'
            WHEN 'AS_BUILT_NOTIF_SUBMITTER' THEN 'AS_BUILT_NOTIFICATION_SUBMITTER'
            WHEN 'TEMPLATE_CLAUSE_MANAGER'  THEN 'TEMPLATE_CLAUSE_MANAGER'
            WHEN 'PWA_ACCESS'               THEN 'PWA_ACCESS'
          END role
        FROM ${datasource.user}.portal_res_memb_current_roles prmcr
        JOIN ${datasource.user}.user_accounts ua ON ua.person_id = prmcr.person_id AND ua.account_status = 'ACTIVE'
        WHERE prmcr.res_id = team_record.res_id
      ) LOOP
        -- Check if the team role already exists to avoid duplicates
        SELECT COUNT(*)
        INTO l_team_role_count
        FROM ${datasource.user}.team_roles t
        WHERE t.team_id = team_role_record.role
          AND t.role = team_role_record.role
          AND t.wua_id = team_role_record.wua_id;

        IF l_team_role_count = 0 THEN
          l_uuid := new_uuid();
          -- Insert the team role
          INSERT INTO ${datasource.user}.team_roles (id, team_id, role, wua_id)
          VALUES (l_uuid, l_team_id, team_role_record.role, team_role_record.wua_id)
          RETURNING id INTO l_team_role_id;
        END IF;
      END LOOP;

    END IF;

  END LOOP;

END;
/
