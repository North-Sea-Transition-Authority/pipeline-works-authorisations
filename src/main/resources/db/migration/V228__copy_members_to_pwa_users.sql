DECLARE

  l_target_res_id decmgr.resources.id%TYPE;
  l_target_res_type decmgr.resource_types.res_type%TYPE := 'PWA_USERS';
  l_system_wua_id appenv.web_user_accounts.id%TYPE := 0; -- System/Admin WUA ID

BEGIN

  -- Find the target team
  BEGIN
	 SELECT r.id
	 INTO l_target_res_id
	 FROM decmgr.resources r
	 WHERE r.res_type = l_target_res_type;
  EXCEPTION
  WHEN NO_DATA_FOUND THEN
	  raise_application_error(-20001, 'Target resource for type not found: ' || l_target_res_type);
  END;

  -- Get all unique members
  FOR member_rec IN (
	  SELECT DISTINCT rmc.person_id
	  FROM decmgr.resource_members_current rmc
	  WHERE rmc.person_id IS NOT NULL
	  AND rmc.res_type IN (
	    'PWA_ORGANISATION_TEAM'
	  , 'PWA_REGULATOR_TEAM'
	  )
  )
  LOOP

	  -- Add member to team and role
	  decmgr.contact.add_member(
	    p_res_id => l_target_res_id
	  , p_role_name => 'PWA_ACCESS'
	  , p_person_id => member_rec.person_id
	  , p_requesting_wua_id => l_system_wua_id
	  );

	  logger.info('PWA Teams Pattern Migration - Copied member with person_id: ' || member_rec.person_id || ' to target resource: ' || l_target_res_id);

  END LOOP;

END;
/