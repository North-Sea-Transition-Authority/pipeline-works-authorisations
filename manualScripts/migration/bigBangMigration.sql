-- RUN as the user required pwa_xx user.
-- replace all instances of pwa_xx with custom schema.
/
DECLARE
  l_total NUMBER;

  l_done NUMBER := 0;
BEGIN

  SELECT COUNT(*)
  INTO l_total
  FROM pwa_mh.mig_master_pwas;

  logger.log('PWA_MH1 Big Bang migration. 0 /' || l_total, 10);

  FOR mig_master_pwa IN (
    SELECT *
    FROM pwa_mh.mig_master_pwas
  )
  LOOP
    pwa_mh.migration.migrate_master(mig_master_pwa);
    l_done := l_done + 1;
    logger.log('PWA_MH1 Big Bang migration. ' || l_done || '/' || l_total, 10);
  END LOOP;

END;
/

SELECT li.*
FROM logmgr.log_items li
WHERE li.time > trunc(sysdate)
AND li.message like 'PWA_MH1%'
ORDER BY li.time DESC
/
SELECT *
FROM pwa_mh.migration_master_logs