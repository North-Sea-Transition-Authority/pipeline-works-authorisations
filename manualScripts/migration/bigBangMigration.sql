/* Formatted on 11/05/2020 10:07:31 (QP5 v5.256.13226.35538) */
-- To run:
-- 1. replace all instances of "pwa_xx" with the base application schema for the enviroment eg."PWA"
-- 2. in toad, connect as the base schema for the enviroment, e.g "PWA".
-- 3. run first statement.
/

DECLARE
   l_total        NUMBER;

   l_done         NUMBER := 0;
   l_log_prefix   VARCHAR2 (4000) := 'PWA_XX Big Bang migration: ';
BEGIN
   SELECT COUNT (*) INTO l_total FROM pwa_xx.mig_master_pwas;

   logger.LOG (l_log_prefix || '0 /' || l_total, 10);

   FOR mig_master_pwa IN (SELECT * FROM pwa_xx.mig_master_pwas)
   LOOP
      pwa_xx.migration.migrate_master (mig_master_pwa);
      l_done := l_done + 1;
      logger.LOG (l_log_prefix || l_done || '/' || l_total, 10);
   END LOOP;
END;
/

/* Run this sql to get an idea of how many consents have been processed and how many still to do. */
SELECT li.*
FROM logmgr.log_items li
WHERE li.time > TRUNC (SYSDATE) AND li.MESSAGE LIKE 'PWA_XX%'
ORDER BY li.time DESC
/

/* Run this sql for detailed info on specific migrations.*/
SELECT * FROM pwa_xx.migration_master_logs