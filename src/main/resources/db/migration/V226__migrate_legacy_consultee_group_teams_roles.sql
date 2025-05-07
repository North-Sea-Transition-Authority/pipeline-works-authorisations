DECLARE
  g_type VARCHAR2(10) := 'CONSULTEE';

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
      cgd.cg_id scope_id
    , cgd.name
    FROM ${datasource.user}.consultee_group_details cgd
    WHERE cgd.tip_flag = 1
  ) LOOP
    -- Check if the team already exists to avoid duplicates
    SELECT COUNT(*)
    INTO l_team_count
    FROM ${datasource.user}.teams t
    WHERE t.type = g_type
      AND t.scope_type = g_type
      AND t.scope_id  = team_record.scope_id;

    IF l_team_count = 0 THEN
      l_uuid := new_uuid();
      -- Insert the team
      INSERT INTO ${datasource.user}.teams (id, type, name, scope_type, scope_id)
      VALUES (l_uuid, g_type, team_record.name, g_type, team_record.scope_id)
      RETURNING id INTO l_team_id;

      FOR team_role_record IN (
        SELECT
          ua.wua_id
        , r.column_value role -- extract each role from the CSV list
        FROM ${datasource.user}.consultee_group_team_members cgtm
        JOIN ${datasource.user}.user_accounts ua ON ua.person_id = cgtm.person_id AND ua.account_status = 'ACTIVE'
        , TABLE(st.split(cgtm.csv_role_list, ',')) r
        WHERE cgtm.cg_id = team_record.scope_id
        -- generate a row for every role in CSV list (based on comma count + 1)
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
