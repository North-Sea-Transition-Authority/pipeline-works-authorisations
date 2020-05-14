/*Assumptions:
  > The app is off, no engines connected or secondary db sessions open for the schema.
*/
-- To run:
-- 1. make sure "PWA" is the correct base schema, else replace "PWA." with "PWA_XX."
-- 2. in toad, connect as the base schema for the enviroment, e.g "PWA".
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

   logger.LOG (l_log_prefix || '0 /' || l_total, 10);

   FOR mig_master_pwa IN (SELECT * FROM PWA.mig_master_pwas)
   LOOP
      PWA.migration.migrate_master (mig_master_pwa);
      l_done := l_done + 1;
      logger.LOG (l_log_prefix || l_done || '/' || l_total, 10);
   END LOOP;
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
BEGIN

  SELECT MAX(mpc.pa_id) + 1
  INTO l_max_pa_id
  FROM PWA.mig_pwa_consents mpc;

  SELECT MAX(mpc.pad_id) + 1
  INTO l_max_pad_id
  FROM PWA.mig_pwa_consents mpc;

  SELECT MAX(xph.pipeline_id) + 1
  INTO l_max_pipeline_id
  FROM decmgr.xview_pipelines_history xph;

  SELECT MAX(xph.pd_id) + 1
  INTO l_max_pipeline_detail_id
  FROM decmgr.xview_pipelines_history xph;


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
-- find cosents which have not been migrated ie, their id is not represented in the new pwa_consents table
WHERE xpad.pad_id IN (
  SELECT XPAD2.PAD_ID
  FROM DECMGR.XVIEW_PIPELINE_AUTH_DETAILS xpad2
  MINUS
  SELECT PC.ID 
  FROM PWA.PWA_CONSENTS pc
)
ORDER BY xpad.pa_id, xpad.pad_id

/
/* Pipeline records not represented in the new system*/
SELECT xph.pipeline_id, xph.pd_id
FROM decmgr.xview_pipelines_history xph
MINUS
SELECT pd.pipeline_id, pd.id
FROM pwa_mh.pipeline_details pd



