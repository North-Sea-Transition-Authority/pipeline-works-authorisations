DECLARE
  -- For each res type resource usage. find the associated resource details, if the current resource detail does not contain an item for every roles defined on the
  l_res_type_mnems bpmmgr.varchar2_list_type;

  -- for a res type, generate an empty roles list for all roles in the res type. This roles list matches the resource detail xml format.
  FUNCTION generate_res_detail_roles_list(p_res_type VARCHAR2)
  RETURN XMLTYPE
  IS
    l_empty_roles_xml XMLTYPE;
  BEGIN

    SELECT
     XMLELEMENT("ROLES_LIST"
       , XMLAGG(XMLELEMENT("ROLE"
       , XMLELEMENT("ROLE_NAME", xrtr.role_name )
       , XMLELEMENT("MEMBERS_LIST")
       ))
    )
    INTO l_empty_roles_xml
    FROM decmgr.xview_resource_type_roles xrtr
    WHERE xrtr.res_type = p_res_type;

    RETURN l_empty_roles_xml;

  END generate_res_detail_roles_list;


  PROCEDURE add_missing_res_detail_roles(
    p_res_id NUMBER
  , p_all_roles_xml XMLTYPE --expected to match output from  'generate_res_detail_roles_list'. NOTE: role names are not validated internally against res_type
  , po_new_resource_detail_id OUT NUMBER -- will be null if no missing roles detected in current resource detail
  )
  IS
    l_res_detail_id_to_update NUMBER;

    l_working_resource_xml XMLTYPE;
    l_resource_xml XMLTYPE;
    l_new_resource_detail_xml XMLTYPE;
    l_save_team_event_output XMLTYPE;
    l_new_resource_detail_id NUMBER;

  BEGIN

    SELECT
      CASE
        -- return rd.id if current resource detail does not contain one or more of the role entries named in the provided p_all_roles_xml
        WHEN XMLEXISTS(
          '$roles/ROLES_LIST/ROLE/ROLE_NAME[not(text() = $xml/RESOURCE_DETAIL/ROLES_LIST/ROLE/ROLE_NAME/text())]'
          PASSING
          rd.xml_data AS "xml"
        , p_all_roles_xml AS "roles"
        )
        THEN rd.id
      END CASE
    INTO l_res_detail_id_to_update
    FROM  decmgr.xview_resources xr
    JOIN decmgr.resource_details rd ON rd.res_id = xr.res_id
    WHERE rd.status_control = 'C'
    AND xr.res_id = p_res_id;

    IF(l_res_detail_id_to_update IS NOT NULL) THEN
      SELECT
        r.xml_data
        -- For each role in the all roles xml loop and if theres no entry in the res detail for the role, add an empty entry.
        , XMLQUERY('
            copy $dom := $xml
            modify(
              for $role in $roles/ROLES_LIST/ROLE return (
                if(not(exists($dom/RESOURCE_DETAIL/ROLES_LIST/ROLE/ROLE_NAME[text() = $role/ROLE_NAME/text()])))
                then (
                  insert node $role as last into $dom/RESOURCE_DETAIL/ROLES_LIST
                )
                else()
              )
            )
            return $dom
          '
          PASSING
          rd.xml_data AS "xml"
          , p_all_roles_xml AS "roles"
          RETURNING CONTENT
        )
      INTO
        l_resource_xml
      , l_new_resource_detail_xml
      FROM decmgr.resources r
      JOIN decmgr.resource_details rd ON rd.res_id = r.id
      WHERE rd.id = l_res_detail_id_to_update
      AND rd.status_control = 'C';

      -- only populate WORKING_RESOURCE elements required for a resource detail update, not a new team.
      -- This mimics that produced by the DEC043L FOX 4 module but customised for only res detail updates, not new teams.
      SELECT
        XMLELEMENT("WORKING_RESOURCE"
          , XMLELEMENT("RESOURCE_ID", p_res_id)
          , XMLELEMENT("RESOURCE_DETAIL_ID", l_res_detail_id_to_update)
          -- RESOURCES
          , l_resource_xml
          -- RESOURCE_DETAIL
          , l_new_resource_detail_xml
          )
      INTO l_working_resource_xml
      FROM dual;

      -- Use the save_team method instead of the contact.add_Role method.
      -- This means we end up with a single new resource detail row instead of one new detail per new role.
      decmgr.contact.save_team(
          p_save_resource_xml => l_working_resource_xml
        , p_requesting_wua_id => 1
        , po_event_return_xml => l_save_team_event_output -- ignore as we are not creating teams from scratch.
        , po_new_rd_id => l_new_resource_detail_id
      );

      po_new_resource_detail_id := l_new_resource_detail_id;

    END IF;

  END add_missing_res_detail_roles;

BEGIN
  -- theres probably a more elegant way to do this
  SELECT res_type
  BULK COLLECT INTO l_res_type_mnems
  FROM (
    SELECT 'PWA_REGULATOR_TEAM' res_type FROM dual
    UNION
    SELECT 'PWA_ORGANISATION_TEAM' res_type FROM dual
  );

  FOR res_type_details IN (SELECT column_value as res_type_mnem FROM TABLE(l_res_type_mnems))
  LOOP
    DECLARE
        l_roles_for_res_type_xml XMLTYPE;
    BEGIN

      dbms_output.PUT_LINE('res_type: ' || res_type_details.res_type_mnem);

      l_roles_for_res_type_xml := generate_res_detail_roles_list(p_res_type => res_type_details.res_type_mnem);
      dbms_output.PUT_LINE('roles_xml: ' || l_roles_for_res_type_xml.getClobVal());

      -- loop all resources for the res type
      FOR res_type_resource IN (
        SELECT xr.res_id
        FROM  decmgr.xview_resources xr
        -- left join as static res types do not have 'usages'
        LEFT JOIN decmgr.resource_usages_current ruc ON ruc.res_id = xr.res_id
        JOIN decmgr.resource_details rd ON rd.res_id = xr.res_id
        WHERE xr.res_type = res_type_details.res_type_mnem
        AND rd.status_control = 'C'
      )
      LOOP
        DECLARE
          l_new_resource_detail_id NUMBER;
        BEGIN

          -- cautious savepoint to saving changes made internally if we didnt save a new resource detail.
          SAVEPOINT before_res_changes;
          -- TODO move this method to decmgr.contact when content script performing as expected.
          add_missing_res_detail_roles(
              p_res_id => res_type_resource.res_id
            , p_all_roles_xml => l_roles_for_res_type_xml
            , po_new_resource_detail_id => l_new_resource_detail_id
            );

          IF(l_new_resource_detail_id IS NOT NULL) THEN
            dbms_output.PUT_LINE('res_id:' || res_type_resource.res_id || ' New res_detail:' || l_new_resource_detail_id);
            COMMIT;
          ELSE
            NULL;
            ROLLBACK TO SAVEPOINT before_res_changes;
          END IF;

       END;
      END LOOP;
    END;
  END LOOP;

END;