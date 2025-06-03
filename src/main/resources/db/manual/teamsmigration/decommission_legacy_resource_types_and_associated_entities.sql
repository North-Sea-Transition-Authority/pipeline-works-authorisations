DECLARE
  l_res_type_check NUMBER;
  l_res_ids bpmmgr.number_list_type;
  l_current_res_type decmgr.resource_types.res_type%TYPE;

  l_res_types_to_delete bpmmgr.varchar2_list_type := bpmmgr.varchar2_list_type(
    'PWA_ORGANISATION_TEAM'
  , 'PWA_REGULATOR_TEAM'
  );

BEGIN

  FOR res_type_idx IN l_res_types_to_delete.FIRST .. l_res_types_to_delete.LAST
  LOOP
    l_current_res_type := l_res_types_to_delete(res_type_idx);

    BEGIN
      SELECT 1
      INTO l_res_type_check
      FROM decmgr.resource_types rt
      WHERE rt.res_type = l_current_res_type;
    EXCEPTION WHEN NO_DATA_FOUND THEN
      raise_application_error(-20001, 'Resources not found for res_type: ' || l_current_res_type);
    END;

    logger.info('PWA Teams Pattern Migration - Began deleting resources for res_type: ' || l_current_res_type);


    SELECT r.id
    BULK COLLECT INTO l_res_ids
    FROM decmgr.resources r
    WHERE r.res_type = l_current_res_type;


    DELETE FROM decmgr.resource_usages ru
    WHERE ru.res_id IN (
      SELECT t.column_value
      FROM TABLE(l_res_ids) t
    );
    logger.info('PWA Teams Pattern Migration - RESOURCE_USAGES: '||SQL%ROWCOUNT||' row(s) deleted for res_type: ' || l_current_res_type);


    DELETE FROM decmgr.resource_details rd
    WHERE rd.res_id IN (
      SELECT t.column_value
      FROM TABLE(l_res_ids) t
    );
    logger.info('PWA Teams Pattern Migration - RESOURCE_DETAILS: '||SQL%ROWCOUNT||' row(s) deleted for res_type: ' || l_current_res_type);


	  DELETE FROM decmgr.resource_roles rr
    WHERE rr.res_id IN (
      SELECT t.column_value
      FROM TABLE(l_res_ids) t
    );
    logger.info('PWA Teams Pattern Migration - RESOURCE_ROLES: '||SQL%ROWCOUNT||' row(s) deleted for res_type: ' || l_current_res_type);


    DELETE FROM decmgr.resources r
    WHERE r.id IN (
      SELECT t.column_value
      FROM TABLE(l_res_ids) t
    );
    logger.info('PWA Teams Pattern Migration - RESOURCES: '||SQL%ROWCOUNT||' row(s) deleted for res_type: ' || l_current_res_type);


    DELETE FROM decmgr.resource_types rt
    WHERE rt.res_type = l_current_res_type;
    logger.info('PWA Teams Pattern Migration - RESOURCE_TYPES: '||SQL%ROWCOUNT||' row(s) deleted for res_type: ' || l_current_res_type);


    logger.info('PWA Teams Pattern Migration - Finished deleting resources for res_type: ' || l_current_res_type);

  END LOOP;

END;
/

COMMIT
/