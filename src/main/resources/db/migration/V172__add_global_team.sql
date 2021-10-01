DECLARE

    l_res_id NUMBER;

BEGIN

    SELECT 1
    INTO l_res_id
    FROM decmgr.resources r
    WHERE r.res_type = 'PWA_USERS';

EXCEPTION WHEN NO_DATA_FOUND THEN

    l_res_id := decmgr.contact.create_default_team(
      p_resource_type => 'PWA_USERS'
    , p_resource_name => 'PWA Users'
    , p_resource_desc => 'PWA Users'
    , p_creating_wua_id => 1
    );

END;