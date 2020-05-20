/*Assumptions:
  > The app is off, no engines connected or secondary db sessions open for the schema.
*/
-- To run:
-- 1. make sure "PWA" is the correct base schema, else replace "PWA." with "PWA_XX."
-- 2. in toad, connect as the base schema for the environment, e.g "PWA".
-- 3. run first anonymous block to migrate data.
-- 4. run the second statement to increment the pwa_sequences based on migration data.
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
END;
/


/* Run this sql to get an idea of how many consents have been processed and how many still to do. */
SELECT li.*
FROM logmgr.log_items li
WHERE li.time > TRUNC (SYSDATE) AND li.MESSAGE LIKE 'PWA.%'
ORDER BY li.time DESC
/

/* Run this sql for detailed info on specific migrations.*/
SELECT * FROM PWA.migration_master_logs
/


/* increment pwa sequences based on migration data */
DECLARE
  l_max_pa_id  NUMBER;

  l_max_pad_id NUMBER;

  l_max_pipeline_id NUMBER;

  l_max_pipeline_detail_id NUMBER;
  
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


  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pwas_id_seq INCREMENT BY ' || l_max_pa_id;

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pwa_consent_id_seq INCREMENT BY ' || l_max_pad_id;

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pipeline_id_seq INCREMENT BY ' || l_max_pipeline_id;

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pipeline_details_id_seq INCREMENT BY ' || l_max_pipeline_detail_id;
  
  
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
  
  -- reset increment value to restore expected behaviour.
  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pwas_id_seq INCREMENT BY 1';

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pwa_consent_id_seq INCREMENT BY 1';

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pipeline_id_seq INCREMENT BY 1';

  EXECUTE IMMEDIATE 'ALTER SEQUENCE PWA.pipeline_details_id_seq INCREMENT BY 1';

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
FROM pwa.migration_pipeline_logs mpl
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
FROM pwa.migration_pipeline_logs mpl
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