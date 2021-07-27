ALTER TABLE ${datasource.user}.as_built_notif_submissions RENAME COLUMN date_laid TO date_work_completed;

UPDATE ${datasource.user}.as_built_notif_submissions SET as_built_status = 'NOT_COMPLETED_IN_CONSENT_TIMEFRAME' WHERE as_built_status = 'NOT_LAID_CONSENT_TIMEFRAME';