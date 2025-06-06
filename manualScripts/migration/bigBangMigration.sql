/*Assumptions:
  > The app is off, no engines connected or secondary db sessions open for the schema.
*/
-- To run:
-- 1. make sure "PWA" is the correct base schema, else replace "PWA." with "PWA_XX."
-- 2. in toad, connect as the base schema for the environment, e.g "PWA".
-- 3. run first anonymous block to migrate data.
-- 4. run the second statement to increment the pwa_sequences based on migration data.
--5 . run third block to create teams for holder org grps created in the new system if required.
/

/* migrate data */
DECLARE
   l_total        NUMBER;

   l_done         NUMBER := 0;
   l_log_prefix   VARCHAR2 (4000) := 'PWA. Big Bang migration: ';
BEGIN
   SELECT COUNT (*) INTO l_total FROM PWA.mig_master_pwas;

   logger.LOG (l_log_prefix || 'Consent Processing ' || '0 /' || l_total, 10);

   FOR mig_master_pwa IN (SELECT * FROM PWA.mig_master_pwas)
   LOOP
      PWA.migration.migrate_master (mig_master_pwa);
      l_done := l_done + 1;
      IF(l_done mod 100 = 0) THEN
        logger.LOG (l_log_prefix || l_done || '/' || l_total, 10);
      END IF;
      
   END LOOP;
   logger.LOG (l_log_prefix || l_done || '/' || l_total, 10);

   l_done := 0;
   SELECT COUNT(*) INTO l_total FROM PWA.mig_pipeline_history;
   logger.LOG (l_log_prefix  || 'Pipeline history Processing '  || l_done || '/' || l_total, 10);

   FOR mig_pipeline_history IN (SELECT * FROM PWA.mig_pipeline_history)
     LOOP
       PWA.migration.migrate_pipeline_history (mig_pipeline_history);
       l_done := l_done + 1;
       IF(l_done mod 500 = 0) THEN
        logger.LOG (l_log_prefix || l_done || '/' || l_total, 10);
      END IF;
      
   END LOOP;
   logger.LOG (l_log_prefix || l_done || '/' || l_total, 10);

   PWA.migration.post_core_data_migrations();
   COMMIT;
END;
/


/* Run this sql to get an idea of how many consents have been processed and how many still to do. */
SELECT li.*
FROM logmgr.log_items li
WHERE li.time > TRUNC (SYSDATE) AND li.MESSAGE LIKE 'PWA.%'
ORDER BY li.time DESC
/

/* Run this sql for detailed info on specific migrations.*/
SELECT ml.*
FROM PWA.migration_master_logs ml
WHERE status = 'FAILED'

/
 -- find migrations where the log message contains
SELECT mpl.*
FROM PWA.MIGRATION_PIPELINE_LOGS mpl
WHERE status = 'FAILED'
AND log_messages LIKE '%ORA-01722%'
ORDER BY mpl.pipeline_id, mpl.pipeline_detail_id
/

-- for consents, get the associated migration log
SELECT mpc.*, ML.LOG_MESSAGES, ML.STATUS
FROM PWA.MIG_PWA_CONSENTS mpc
LEFT JOIN PWA.migration_master_logs ml ON ML.MIG_MASTER_PA_ID = mpc.pa_id
WHERE  MPC.PAD_ID IN (:pad_id)


/


/* increment pwa sequences based on migration data */
DECLARE
  l_max_pa_id  NUMBER;

  l_max_pad_id NUMBER;

  l_max_pipeline_id NUMBER;

  l_max_pipeline_detail_id NUMBER;

  l_max_pipeline_number NUMBER;
  
  l_next_val NUMBER;
  
  l_curr_val NUMBER;
  
  e_seq_check EXCEPTION;
  
  PRAGMA EXCEPTION_INIT(e_seq_check, -08002);
   
BEGIN

  BEGIN
  -- we dont want to do this twice by accident
  SELECT PWA.pwas_id_seq.CURRVAL
  INTO l_curr_val
  FROM dual;
  
  RAISE_APPLICATION_ERROR(-20123, 'This should be thrown when the sequence is initialised and we have run the script previously');
  
  EXCEPTION WHEN e_seq_check THEN
  -- do nothing when the "sequence not inited" error is thrown as it is expected on first run.
    NULL;
  END;
  
  IF(l_curr_val > 1) THEN
    RAISE_APPLICATION_ERROR(-20123, 'Sequences already updated!  curr_val:' || l_curr_val);
  END IF;
  -- use migration data, NOT migrated data, for max ids. need to cover cases where there have been migration failures and allow for later data fixes.
  
  
  SELECT MAX(mpc.pa_id) + 1
  INTO l_max_pa_id
  FROM PWA.mig_pwa_consents mpc;

  SELECT MAX(mpc.pad_id) + 1
  INTO l_max_pad_id
  FROM PWA.mig_pwa_consents mpc;

  SELECT MAX(mph.pipeline_id) + 1
  INTO l_max_pipeline_id
  FROM PWA.mig_pipeline_history mph;

  SELECT MAX(mph.pd_id) + 1
  INTO l_max_pipeline_detail_id
  FROM PWA.mig_pipeline_history mph;

  SELECT pmc.reserved_pipeline_number_max
  INTO l_max_pipeline_number
  FROM PWA.pipeline_migration_config pmc;


  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pwas_id_seq INCREMENT BY ' || l_max_pa_id;

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pwa_consent_id_seq INCREMENT BY ' || l_max_pad_id;

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pipeline_id_seq INCREMENT BY ' || l_max_pipeline_id;

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pipeline_details_id_seq INCREMENT BY ' || l_max_pipeline_detail_id;

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pipeline_numbering_seq INCREMENT BY ' || l_max_pipeline_number;
  
  
  -- select next val so the sequences update
  SELECT PWA.pwas_id_seq.NEXTVAL
  INTO l_next_val
  FROM dual;

  SELECT PWA.pwa_consent_id_seq.NEXTVAL
  INTO l_next_val
  FROM dual;

  SELECT PWA.pipeline_id_seq.NEXTVAL
  INTO l_next_val
  FROM dual;

  SELECT PWA.pipeline_details_id_seq.NEXTVAL
  INTO l_next_val
  FROM dual;

  SELECT PWA.pipeline_numbering_seq.NEXTVAL
  INTO l_next_val
  FROM dual;
  
  -- reset increment value to restore expected behaviour.
  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pwas_id_seq INCREMENT BY 1';

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pwa_consent_id_seq INCREMENT BY 1';

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pipeline_id_seq INCREMENT BY 1';

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pipeline_details_id_seq INCREMENT BY 1';

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pipeline_numbering_seq INCREMENT BY 1';

END;

/
/* Sanity check pipeline authorisation details not migrated into the new service. */
SELECT xpad.*
, PA.FIRST_PAD_ID
, (SELECT count(*) FROM DECMGR.XVIEW_PIPELINE_AUTH_DETAILS xpad3 WHERE xpad3.pa_id = xpad.pa_id) total_pa_consents
, (SELECT count(*) FROM DECMGR.XVIEW_PIPELINES_HISTORY xph WHERE XPH.PIPE_AUTH_DETAIL_ID = xpad.pad_id ) total_hist_pipelines_on_pad
FROM DECMGR.XVIEW_PIPELINE_AUTH_DETAILS xpad
JOIN DECMGR.PIPELINE_AUTHORISATIONS pa ON PA.ID = xpad.pa_id
-- find consents which have not been migrated ie, their id is not represented in the new pwa_consents table
WHERE xpad.pad_id IN (
  SELECT XPAD2.PAD_ID
  FROM DECMGR.XVIEW_PIPELINE_AUTH_DETAILS xpad2
  MINUS
  SELECT PC.ID 
  FROM PWA.PWA_CONSENTS pc
)
ORDER BY xpad.pa_id, xpad.pad_id
/

-- get total pipeline history failures per pad where pad migration failed
SELECT pad_id, status, count(*)
FROM PWA.migration_pipeline_logs mpl
WHERE pad_id IN(
  SELECT xpad.pad_id
  FROM DECMGR.XVIEW_PIPELINE_AUTH_DETAILS xpad
  JOIN DECMGR.PIPELINE_AUTHORISATIONS pa ON PA.ID = xpad.pa_id
  -- find consents which have not been migrated ie, their id is not represented in the new pwa_consents table
  WHERE xpad.pad_id IN (
    SELECT XPAD2.PAD_ID
    FROM DECMGR.XVIEW_PIPELINE_AUTH_DETAILS xpad2
    MINUS
    SELECT PC.ID 
    FROM PWA.PWA_CONSENTS pc
  )
)
GROUP BY ROLLUP(pad_id, status)

/
-- total pipeline record migrations failures where consent migration succeeded
/
SELECT pad_id, status, count(*)
FROM PWA.migration_pipeline_logs mpl
WHERE pad_id NOT IN(
  SELECT xpad.pad_id
  FROM DECMGR.XVIEW_PIPELINE_AUTH_DETAILS xpad
  JOIN DECMGR.PIPELINE_AUTHORISATIONS pa ON PA.ID = xpad.pa_id
  -- find consents which have not been migrated ie, their id is not represented in the new pwa_consents table
  WHERE xpad.pad_id IN (
    SELECT XPAD2.PAD_ID
    FROM DECMGR.XVIEW_PIPELINE_AUTH_DETAILS xpad2
    MINUS
    SELECT PC.ID 
    FROM PWA.PWA_CONSENTS pc
  )
)
AND mpl.status = 'FAILED'
GROUP BY ROLLUP(pad_id, status)

/

/*
 Run this to create holder portal teams if they dont exist.
 */
DECLARE
  l_dryrun BOOLEAN := false;
BEGIN

  FOR orggrp IN (
    SELECT DISTINCT cog.id, cog.org_grp_type, cog.name, cog.short_name, cogo.org_grp_id || '++REGORGGRP' uref
    FROM pwa.pwa_consent_organisation_roles pcor
    JOIN decmgr.current_org_grp_organisations cogo ON pcor.ou_id = pcor.ou_id
    JOIN decmgr.current_organisation_groupS cog ON cogo.org_grp_id = cog.id ANd cog.org_grp_type = cogo.org_grp_type AND COG.ORG_GRP_TYPE ='REG'
    LEFT JOIN decmgr.resource_usages_current ruc ON ruc.uref = cogo.org_grp_id || '++REGORGGRP'
    LEFT JOIN decmgr.xview_resources xr ON ruc.res_id = xr.res_id AND xr.res_name = 'PWA_ORGANISATION_TEAM'
    WHERE pcor.role = 'HOLDER'
    AND pcor.ended_by_pwa_consent_id IS NULL
    AND ruc.res_id IS NULL
    ) LOOP
      DECLARE
        l_res_id NUMBER;
      BEGIN
        dbms_output.put('Creating Missing orggrp team: ' || orggrp.name || '(' || orggrp.id || ')' );
        IF(NOT l_dryrun) THEN
          pwa.team_management.create_team(
              p_resource_type => 'PWA_ORGANISATION_TEAM'
            , p_resource_name => 'PWA Holder Team for ' || orggrp.short_name
            , p_resource_description => 'PWA Holder Team for ' || orggrp.name
            , p_uref => orggrp.uref
            , p_requesting_wua_id => 1
            , po_resource_id => l_res_id
            );

          COMMIT;
          DBMS_OUTPUT.PUT_LINE('... Committed (res_id: ' || l_res_id || ')');
        ELSE
          DBMS_OUTPUT.PUT_LINE('... Skipped');
        END IF;

      END;
    END LOOP;

END;
