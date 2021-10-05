DECLARE

    l_person_id_list bpmmgr.number_list_type;
    l_res_id NUMBER;

BEGIN

    WITH people AS (
        SELECT pac.person_id
        FROM pwa.pwa_application_contacts pac
        UNION
        SELECT person_id
        FROM pwa.consultee_group_team_members cgtm
    )
    SELECT p.person_id
            BULK COLLECT INTO l_person_id_list
    FROM people p
    WHERE p.person_id NOT IN (
        SELECT DISTINCT rmc.person_id
        FROM decmgr.resource_members_current rmc
        WHERE rmc.res_type IN ('PWA_REGULATOR_TEAM', 'PWA_ORGANISATION_TEAM', 'PWA_USERS')
    );

    SELECT r.id
    INTO l_res_id
    FROM decmgr.resources r
    WHERE r.res_type = 'PWA_USERS';

    decmgr.contact.add_members_to_roles(
      p_res_id => l_res_id
    , p_role_name_list => bpmmgr.varchar2_list_type('PWA_ACCESS')
    , p_person_id_list => l_person_id_list
    , p_requesting_wua_id => 1
    );

END;