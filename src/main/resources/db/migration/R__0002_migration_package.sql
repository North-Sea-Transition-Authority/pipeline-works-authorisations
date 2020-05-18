CREATE OR REPLACE PACKAGE ${datasource.user}.migration AS

  initial_pwa VARCHAR2(4000) := 'INITIAL_PWA';
  variation VARCHAR2(4000) := 'VARIATION';
  deposit_consent VARCHAR2(4000) := 'DEPOSIT_CONSENT';

  e_migration_found EXCEPTION;
  e_num_migration_found NUMBER := -20999;
  PRAGMA EXCEPTION_INIT(e_migration_found, -20999);


  PROCEDURE migrate_master(p_mig_master_pwa ${datasource.user}.mig_master_pwas%ROWTYPE);

END migration;
/

CREATE OR REPLACE PACKAGE BODY ${datasource.user}.migration
AS

  PROCEDURE previous_migration_check(p_mig_master_pwa ${datasource.user}.mig_master_pwas%ROWTYPE)
  AS
    l_count NUMBER;
  BEGIN
    SELECT count(*)
    INTO l_count
    FROM ${datasource.user}.migrated_pipeline_auths mpa
    WHERE mpa.pad_id = p_mig_master_pwa.pad_id;

    IF(l_count !=0) THEN
      RAISE_APPLICATION_ERROR(e_num_migration_found, 'Master pwa with padId:' || p_mig_master_pwa.pad_id || ' already migrated');
    END IF;
  END previous_migration_check;

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


  FUNCTION determine_consent_type(p_mig_consent ${datasource.user}.mig_pwa_consents%ROWTYPE)
  RETURN VARCHAR2 AS
  BEGIN
    IF(is_initial_pwa_consent(p_mig_consent) ) THEN
     RETURN initial_pwa;
    END IF;

    IF(p_mig_consent.variation_number IS NULL ) THEN
      RETURN deposit_consent;
    END IF;

    IF (p_mig_consent.variation_number IS NOT NULL) THEN
      RETURN variation;
    END IF;

    RETURN NULL;

  END determine_consent_type;


  FUNCTION create_consent_migration(p_mig_master_pwa ${datasource.user}.mig_master_pwas%ROWTYPE
                                   , p_mig_consent ${datasource.user}.mig_pwa_consents%ROWTYPE)
    RETURN ${datasource.user}.migrated_pipeline_auths%ROWTYPE
  AS
    l_consent_type VARCHAR2(4000);
    l_new_consent ${datasource.user}.pwa_consents%ROWTYPE;
    l_new_migrated_consent_auth ${datasource.user}.migrated_pipeline_auths%ROWTYPE;
    l_timestamp TIMESTAMP := SYSTIMESTAMP;
    l_mig_auth_id NUMBER;

  BEGIN
    ${datasource.user}.migration_logger.log(p_mig_master_pwa, 'IN_PROGRESS',
                                'Start migration of consent pad_id:' || p_mig_consent.pad_id || ' reference:' || p_mig_consent.reference);
    -- Sanity check data integrity
    IF(p_mig_master_pwa.pa_id != p_mig_consent.pa_id)THEN
      RAISE_APPLICATION_ERROR(-20904, 'pa_id mismatch for master pad_id: ' || p_mig_master_pwa.pad_id  || '  and consent pad_id:' || p_mig_consent.pad_id);
    END IF;

    l_consent_type := determine_consent_type(p_mig_consent);
    -- Sanity check types
    IF(l_consent_type NOT IN (initial_pwa, variation, deposit_consent) OR l_consent_type IS NULL) THEN
      RAISE_APPLICATION_ERROR(-20904, 'Could not determine pad_id:' || p_mig_consent.pad_id || ' concept type:' || l_consent_type);
    END IF;

    IF(p_mig_consent.consent_date IS NULL) THEN
      RAISE_APPLICATION_ERROR(-20904, 'Cannot create consent when consent date is Null! padId:' || p_mig_consent.pad_id);
    END IF;

    l_new_consent.pwa_id := p_mig_consent.pa_id;
    l_new_consent.id := p_mig_consent.pad_id;
    l_new_consent.reference := p_mig_consent.reference;
    l_new_consent.variation_number := p_mig_consent.variation_number;
    l_new_consent.created_timestamp := l_timestamp;
    l_new_consent.consent_type := l_consent_type;
    l_new_consent.consent_timestamp := p_mig_consent.consent_date;
    l_new_consent.is_migrated_flag := 1;

    INSERT INTO ${datasource.user}.pwa_consents VALUES l_new_consent;

    INSERT INTO ${datasource.user}.migrated_pipeline_auths (pad_id, pwa_pipeline_consent_id, migrated_timestamp, migration_type, pa_id)
    VALUES (p_mig_consent.pad_id, l_new_consent.id, l_timestamp, l_consent_type, p_mig_consent.pa_id)
    RETURNING id INTO l_mig_auth_id;

    SELECT *
    INTO l_new_migrated_consent_auth
    FROM ${datasource.user}.migrated_pipeline_auths mpa
    WHERE  mpa.id = l_mig_auth_id;

    ${datasource.user}.migration_logger.log(p_mig_master_pwa, 'IN_PROGRESS',
                                'Completed migration of consent pad_id:' || p_mig_consent.pad_id || 'Type:' || l_consent_type);

    RETURN l_new_migrated_consent_auth;

  END create_consent_migration;


  FUNCTION create_master_pwa(p_mig_master_pwa ${datasource.user}.mig_master_pwas%ROWTYPE
                            , p_initial_pwa_consent ${datasource.user}.mig_pwa_consents%ROWTYPE)
    RETURN ${datasource.user}.pwa_details%ROWTYPE
  AS
    l_master_pwa_id NUMBER;
    l_master_pwa_detail_id NUMBER;

    l_inserted_pwa_detail ${datasource.user}.pwa_details%ROWTYPE;
  BEGIN
    pad_already_migrated_check(p_initial_pwa_consent);

    -- sanity check that this does look like an initial PWA
    IF NOT (is_initial_pwa_consent(p_initial_pwa_consent))
    THEN
      RAISE_APPLICATION_ERROR(-209002,
                              'pad id does not match expected INITIAL_PWA format. pad_id:' || p_mig_master_pwa.pad_id);
    END IF;

    -- existing pa_id is the entire PWA, and subsequent authorisations are linked as a new pad_id detail to the single pa_id
    -- this relationship between "concept" pwa and individual authorisations(consents) is to be maintained.
    INSERT INTO ${datasource.user}.pwas (id, created_timestamp)
    VALUES (p_mig_master_pwa.pa_id, SYSTIMESTAMP)
    RETURNING id INTO l_master_pwa_id;

    ${datasource.user}.migration_logger.log(p_mig_master_pwa, 'IN_PROGRESS', 'Master pwa inserted. MASTER_PWA_ID:' || l_master_pwa_id);

    -- the PWA reference does not seem to have any relationship with the base ids.
    INSERT INTO ${datasource.user}.pwa_details (pwa_id, pwa_status, reference, start_timestamp)
    VALUES (l_master_pwa_id, 'CONSENTED', p_initial_pwa_consent.reference, SYSTIMESTAMP)
    RETURNING id INTO l_master_pwa_detail_id;

    ${datasource.user}.migration_logger.log(p_mig_master_pwa, 'IN_PROGRESS',
                                'Master pwa detail inserted. MASTER_PWA_DETAIL_ID:' || l_master_pwa_detail_id);

    SELECT *
    INTO l_inserted_pwa_detail
    FROM ${datasource.user}.pwa_details pd
    WHERE pd.id = l_master_pwa_detail_id;

    RETURN l_inserted_pwa_detail;

  END create_master_pwa;

  PROCEDURE migrate_master(p_mig_master_pwa ${datasource.user}.mig_master_pwas%ROWTYPE)
  AS
    l_master_mig_pwa_consent ${datasource.user}.mig_pwa_consents%ROWTYPE;
    l_master_pwa_detail ${datasource.user}.pwa_details%ROWTYPE;
    l_total_consents NUMBER;
  BEGIN
    /*
    * Migrating consents:
    * 1. Create Master PWA
    *   - pa_id should be set as master_pwa_id to maintain current id relationships to ease system integration.
    * 2. Loop over all the consents related to the migrations master
    *   - pad_id should be set as the pwa_consent id to ease system integration
    */

    l_master_mig_pwa_consent := get_mig_pwa_consent(p_mig_master_pwa.pad_id);
    ${datasource.user}.migration_logger.log(p_mig_master_pwa, 'STARTING',
        'Starting migration. Initial PWA reference: ' || l_master_mig_pwa_consent.reference);
    previous_migration_check(p_mig_master_pwa);

    l_master_pwa_detail := create_master_pwa(
        p_mig_master_pwa => p_mig_master_pwa
      , p_initial_pwa_consent => l_master_mig_pwa_consent
    );

    ${datasource.user}.migration_logger.log(p_mig_master_pwa, 'IN_PROGRESS', 'Master pwa migration completed');

    SELECT COUNT(*)
    INTO l_total_consents
    FROM ${datasource.user}.mig_pwa_consents mpc
    WHERE mpc.pa_id = p_mig_master_pwa.pa_id;

    ${datasource.user}.migration_logger.log(p_mig_master_pwa, 'IN_PROGRESS', 'Consents that need migrating: ' || l_total_consents);

    FOR consent_to_migrate IN (
      SELECT *
      FROM ${datasource.user}.mig_pwa_consents mpc
      WHERE mpc.pa_id = p_mig_master_pwa.pa_id
       ORDER BY mpc.consent_date ASC NULLS LAST
    )
    LOOP
      DECLARE
        l_auth_migration ${datasource.user}.migrated_pipeline_auths%ROWTYPE;
      BEGIN
        l_auth_migration := create_consent_migration(
          p_mig_master_pwa => p_mig_master_pwa
          , p_mig_consent => consent_to_migrate
        );
      END;
    END LOOP;

    COMMIT;
    ${datasource.user}.migration_logger.log(p_mig_master_pwa, 'SUCCESS', 'All migration steps completed and committed');

  EXCEPTION
    WHEN e_migration_found THEN
      ROLLBACK;
      ${datasource.user}.migration_logger.log(p_mig_master_pwa, 'SKIPPED', SQLERRM || CHR(10) || dbms_utility.format_error_backtrace());
    WHEN OTHERS THEN
      ROLLBACK;
      ${datasource.user}.migration_logger.log(p_mig_master_pwa, 'FAILED', SQLERRM || CHR(10) || dbms_utility.format_error_backtrace());

  END migrate_master;

END migration;
/

