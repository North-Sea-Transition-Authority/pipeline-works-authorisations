CREATE OR REPLACE PACKAGE ${datasource.user}.migration AS

  initial_pwa VARCHAR2(4000) := 'INITIAL_PWA';
  variation VARCHAR2(4000) := 'VARIATION';
  deposit_consent VARCHAR2(4000) := 'DEPOSIT_CONSENT';

  PROCEDURE migrate_master(p_mig_master_pwa ${datasource.user}.mig_master_pwas%ROWTYPE);

  FUNCTION updated_log_message(p_old_status VARCHAR2
                              , p_new_status VARCHAR2
                              , p_message VARCHAR2
                              , p_clob_log CLOB)
    RETURN CLOB;

  FUNCTION formatted_log_prefix(p_old_status VARCHAR2
                               , p_new_status VARCHAR2)
    RETURN VARCHAR2;

END migration;
/

CREATE OR REPLACE PACKAGE BODY ${datasource.user}.migration
AS
  FUNCTION formatted_log_prefix(p_old_status VARCHAR2
                               , p_new_status VARCHAR2)
    RETURN VARCHAR2 AS
  BEGIN
    -- microseconds to 4 decimal places
    RETURN TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:mm:ssXFF4') || '; ' || p_old_status || ' -> ' || p_new_status || '; ';
  END formatted_log_prefix;

  FUNCTION updated_log_message(p_old_status VARCHAR2
                              , p_new_status VARCHAR2
                              , p_message VARCHAR2
                              , p_clob_log CLOB)
    RETURN CLOB
  AS
    l_log_prefix VARCHAR2(4000) := CHR(10) || CHR(10) || formatted_log_prefix(p_old_status, p_new_status);
  BEGIN

    RETURN p_clob_log || l_log_prefix || p_message;

  END updated_log_message;

  PROCEDURE log(p_mig_master_pwa ${datasource.user}.mig_master_pwas%ROWTYPE
               , p_status VARCHAR2
               , p_message VARCHAR2)
  AS
    PRAGMA AUTONOMOUS_TRANSACTION;
    l_log_id      NUMBER;
  BEGIN

    BEGIN
      SELECT mml.id
      INTO l_log_id
      FROM ${datasource.user}.migration_master_logs mml
      WHERE mml.mig_master_pa_id = p_mig_master_pwa.pa_id;
    EXCEPTION
      WHEN no_data_found THEN
        INSERT INTO ${datasource.user}.migration_master_logs ( mig_master_pa_id
                                                 , status
                                                 , log_messages)
        VALUES ( p_mig_master_pwa.pa_id
               , 'CREATED'
               , formatted_log_prefix('NONE', 'CREATED') || ' ')
        RETURNING id INTO l_log_id;
    END;

    UPDATE ${datasource.user}.migration_master_logs mml
    SET mml.last_updated = SYSTIMESTAMP
      , mml.status       = p_status
      , mml.log_messages = updated_log_message(
        p_old_status => mml.status
      , p_new_status => p_status
      , p_message => p_message
      , p_clob_log => mml.log_messages
      )
    WHERE mml.id = l_log_id;

    COMMIT;

  END log;

  PROCEDURE pad_already_migrated_check(p_mig_consent ${datasource.user}.mig_pwa_consents%ROWTYPE)
  AS
    l_count NUMBER;
  BEGIN

    SELECT count(*)
    INTO l_count
    FROM ${datasource.user}.migrated_pipeline_auths mpa
    WHERE mpa.pad_id = p_mig_consent.pad_id;

    IF (l_count != 0)
    THEN
      RAISE_APPLICATION_ERROR(-20901, 'Already migrated pad_id:' || p_mig_consent.pad_id);
    END IF;

    SELECT count(*)
    INTO l_count
    FROM ${datasource.user}.pwa_details pd
    WHERE pd.reference = p_mig_consent.reference;

    IF (l_count != 0)
    THEN
      RAISE_APPLICATION_ERROR(-20901, 'Master consent with reference already exists pad_id:' || p_mig_consent.pad_id);
    END IF;
  END;

  FUNCTION get_mig_pwa_consent(p_pad_id NUMBER)
    RETURN ${datasource.user}.mig_pwa_consents%ROWTYPE
  AS
    l_return ${datasource.user}.mig_pwa_consents%ROWTYPE;
  BEGIN

    SELECT *
    INTO l_return
    FROM ${datasource.user}.mig_pwa_consents mpc
    WHERE mpc.pad_id = p_pad_id;

    RETURN l_return;

  END get_mig_pwa_consent;

  FUNCTION is_initial_pwa_consent(p_mig_pwa_consent ${datasource.user}.mig_pwa_consents%ROWTYPE)
    RETURN BOOLEAN
  AS
  BEGIN
    RETURN p_mig_pwa_consent.variation_number = 0 AND p_mig_pwa_consent.first_pad_id = p_mig_pwa_consent.pad_id;
  END is_initial_pwa_consent;

  PROCEDURE migrate_master(p_mig_master_pwa ${datasource.user}.mig_master_pwas%ROWTYPE)
  AS
    l_master_mig_pwa_consent ${datasource.user}.mig_pwa_consents%ROWTYPE;
    l_master_pwa_id          NUMBER;
    l_master_pwa_detail_id   NUMBER;
  BEGIN
    /*
    * Migrating consents:
    * 1. Create the INITIAL_PWA consent from the migration master
    * 2. add this to the processed migrations list to log work done and avoid duplicating migration work // TODO PWA-467
    * 3. find all the consents related to the migrations master, this will include a record for the INITIAL PWA already migrated // TODO PWA-467
    * 4. remove the INITIAL PWA consent from the consentsToMigrate list as it is already done // TODO PWA-467
    * 5. loop over remaining "migratable" consents which should be deposit or variation consents only, // TODO PWA-467
    *    creating each one in the system, and adding a ProcessedPwaConsentMigration to the list. //TODO PWA-467
    */

    l_master_mig_pwa_consent := get_mig_pwa_consent(p_mig_master_pwa.pad_id);
    log(p_mig_master_pwa, 'STARTING',
        'Starting migration. Initial PWA reference: ' || l_master_mig_pwa_consent.reference);
    pad_already_migrated_check(p_mig_master_pwa);

    -- sanity check that this does look like an initial PWA
    IF NOT (is_initial_pwa_consent(l_master_mig_pwa_consent))
    THEN
      RAISE_APPLICATION_ERROR(-209002,
                              'pad id does not match expected INITIAL_PWA format. pad_id:' || p_mig_master_pwa.pad_id);
    END IF;

    -- consent reference does not correspond to pa_id or pad_id. so have not bothered to set the master_pwa_id to be one of those.
    INSERT INTO ${datasource.user}.pwas (created_timestamp)
    VALUES (SYSTIMESTAMP)
    RETURNING id INTO l_master_pwa_id;

    log(p_mig_master_pwa, 'IN_PROGRESS', 'Master pwa inserted. MASTER_PWA_ID:' || l_master_pwa_id);

    INSERT INTO ${datasource.user}.pwa_details (pwa_id, pwa_status, reference, start_timestamp)
    VALUES (l_master_pwa_id, 'CONSENTED', l_master_mig_pwa_consent.reference, SYSTIMESTAMP)
    RETURNING id INTO l_master_pwa_detail_id;

    log(p_mig_master_pwa, 'IN_PROGRESS',
        'Master pwa detail inserted. MASTER_PWA_DETAIL_ID:' || l_master_pwa_detail_id);

    COMMIT;
    log(p_mig_master_pwa, 'SUCCESS', 'All migration steps completed and committed');

  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      log(p_mig_master_pwa, 'FAILED', SQLERRM || CHR(10) || dbms_utility.format_error_backtrace());

  END migrate_master;

END migration;
/

