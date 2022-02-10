BEGIN
  UPDATE ${datasource.user}.qrtz_job_details
  SET job_class_name = 'uk.co.ogauthority.pwa.features.appprocessing.processingcharges.jobs.PaymentAttemptCleanupBean'
  WHERE job_class_name = 'uk.co.ogauthority.pwa.service.appprocessing.processingcharges.jobs.PaymentAttemptCleanupBean';

  IF(SQL%ROWCOUNT != 1) THEN
    RAISE_APPLICATION_ERROR(-20123, 'Expected 1 row updated. Actual:' || SQL%ROWCOUNT);
  END IF;
  COMMIT;
END;