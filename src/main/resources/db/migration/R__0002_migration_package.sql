CREATE OR REPLACE PACKAGE ${datasource.user}.migration
AS

  initial_pwa VARCHAR2(4000) := 'INITIAL_PWA';
  variation VARCHAR2(4000) := 'VARIATION';
  deposit_consent VARCHAR2(4000) := 'DEPOSIT_CONSENT';

  e_migration_found EXCEPTION;
  e_num_migration_found NUMBER := -20999;
  PRAGMA EXCEPTION_INIT (e_migration_found, -20999);

  PROCEDURE migrate_master(p_mig_master_pwa ${datasource.user}.mig_master_pwas%ROWTYPE);

  PROCEDURE migrate_pipeline_history(p_mig_pipeline_history ${datasource.user}.mig_pipeline_history%ROWTYPE);

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

    IF (l_count != 0) THEN
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

  FUNCTION get_last_consented_consent_id(p_master_pwa_id NUMBER)
    RETURN NUMBER
  AS
    l_last_consented_consent_id NUMBER;
  BEGIN
    -- get the last consent on the pwa, as we are dealing only with tip data, doesnt really matter for our purposes if it was a deposit consent,
    -- because the data can only tell us the state of the world at the time of the last consent.
    -- as last resort order by id if multiple consents at same exact time
    WITH ranked_consents AS (
      SELECT
        pc.id
      , RANK() OVER (ORDER BY pc.consent_timestamp DESC, pc.id DESC) rank
      FROM ${datasource.user}.pwa_consents pc
      WHERE pc.pwa_id = p_master_pwa_id
    )
    SELECT id
    INTO l_last_consented_consent_id
    FROM ranked_consents
    WHERE rank = 1;

    RETURN l_last_consented_consent_id;

  END get_last_consented_consent_id;

  /** when creating a pipeline, at this point create the overall App level pipeline HUOO data.
    Historical HUOO data does not need to be loaded into app tables (concerns about effort, and meaningfullness of data if attempted)
    Historical HUOO data loaded at pipeline detail level for reference, not into application significant huoo tables
  */
  FUNCTION get_or_create_pipeline(p_mig_pipeline_history ${datasource.user}.mig_pipeline_history%ROWTYPE
                                 , p_dry_run BOOLEAN DEFAULT FALSE
  )
    RETURN ${datasource.user}.pipelines%ROWTYPE
  AS
    l_pipeline_row             ${datasource.user}.pipelines%ROWTYPE;
    l_master_pwa_id            NUMBER;
    l_latest_consent_id        NUMBER;
    l_count                    NUMBER;
  BEGIN

    SAVEPOINT dry_run_safety_net;
    /**
      When creating the main pipeline, we need to just use the status control = 'C' consent.
      Assumption is that the consent already exists at the point pipelines start getting migrated.
     */
    SELECT COUNT(*)
    INTO l_count
    FROM ${datasource.user}.pipelines p
    WHERE p.id = p_mig_pipeline_history.pipeline_id;

    IF (l_count = 0) THEN
      -- pa_id of consents is the master pwa in new system is mapped to
      SELECT mpa.pa_id
      INTO l_master_pwa_id
      FROM ${datasource.user}.mig_pipeline_history mph
      JOIN ${datasource.user}.migrated_pipeline_auths mpa ON mph.pipe_auth_detail_id = mpa.pad_id
      WHERE mph.status_control = 'C'
      AND mph.pipeline_id = p_mig_pipeline_history.pipeline_id;

      l_latest_consent_id := get_last_consented_consent_id(l_master_pwa_id);

      INSERT INTO ${datasource.user}.pipelines(id, pwa_id)
      VALUES (p_mig_pipeline_history.pipeline_id, l_master_pwa_id);

      -- for the current pipeline record, find matching consent level org role
      FOR current_pipeline_huoo_roles IN (
        SELECT mphor.pipeline_id, mphor.role, mphor.org_ou_id, mphor.org_manual_name
        FROM ${datasource.user}.mig_pipeline_hist_org_roles mphor
        WHERE mphor.status_control = 'C'
        AND mphor.pipeline_status != 'DELETED'
        AND mphor.pa_id = l_master_pwa_id
        AND mphor.pipeline_id = p_mig_pipeline_history.pipeline_id
      ) LOOP

        DECLARE
          l_matching_consent_org_role ${datasource.user}.pwa_consent_organisation_roles%ROWTYPE;
        BEGIN
          -- if this does not return a row then the dry run will blow up same as the normal run, and the linked consent will not get migrated
          SELECT *
          INTO l_matching_consent_org_role
          FROM ${datasource.user}.pwa_consent_organisation_roles cor
          -- consent level huoo only linked to latest consent on pwa
          WHERE cor.added_by_pwa_consent_id = l_latest_consent_id
          AND cor.role = current_pipeline_huoo_roles.role
          AND (cor.ou_id = current_pipeline_huoo_roles.org_ou_id
                 OR (cor.ou_id IS NULL AND current_pipeline_huoo_roles.org_ou_id IS NULL)
            )
          AND (cor.migrated_organisation_name = current_pipeline_huoo_roles.org_manual_name
                 OR (cor.migrated_organisation_name IS NULL AND current_pipeline_huoo_roles.org_manual_name IS NULL)
            );

            INSERT INTO ${datasource.user}.pipeline_org_role_links( pipeline_id
                                                                  , pwa_consent_org_role_id -- this links the pipeline huoo role to the consent huoo role
                                                                  , added_by_pwa_consent_id -- this links the pipeline huoo role to the consent which added the link
                                                                  , start_timestamp
                                                                  )
            VALUES( p_mig_pipeline_history.pipeline_id
                  , l_matching_consent_org_role.id
                  , l_matching_consent_org_role.added_by_pwa_consent_id
                  , SYSTIMESTAMP


            );
          END;
        END LOOP;

      IF(p_dry_run) THEN
        ROLLBACK TO SAVEPOINT dry_run_safety_net;

        ${datasource.user}.migration_logger.log_pipeline(
            p_mig_pipeline_history => p_mig_pipeline_history
          , p_status => 'DRY_RUN_COMPLETE'
          , p_message => 'DRY RUN Master pipeline creation pipeline_id:' || p_mig_pipeline_history.pipeline_id);
        RETURN NULL;
      ELSE
        ${datasource.user}.migration_logger.log_pipeline(
            p_mig_pipeline_history => p_mig_pipeline_history
          , p_status => ${datasource.user}.migration_logger.master_pipeline_created_status
          , p_message => 'Master pipeline created. pipeline_id:' || p_mig_pipeline_history.pipeline_id);
      END IF;

    END IF;

    SELECT *
    INTO l_pipeline_row
    FROM ${datasource.user}.pipelines p
    WHERE p.id = p_mig_pipeline_history.pipeline_id;

    RETURN l_pipeline_row;

  END get_or_create_pipeline;

  PROCEDURE migrate_pipeline_history(p_mig_pipeline_history ${datasource.user}.mig_pipeline_history%ROWTYPE)
  AS
    l_master_pipeline_row      ${datasource.user}.pipelines%ROWTYPE;
    l_detail_id                NUMBER;
    l_detail_ident_id          NUMBER;
    l_detail_ident_data_id     NUMBER;
    l_detail_migration_data_id NUMBER;
    l_huoo_role_count NUMBER := 0;

    l_length_metre NUMBER;

  BEGIN
    ${datasource.user}.migration_logger.log_pipeline(
        p_mig_pipeline_history => p_mig_pipeline_history
      , p_status => 'START'
      , p_message => 'pipeline record migration started pd_id: ' || p_mig_pipeline_history.pd_id
      );

    /* This needs to create the master pipeline record and link current HUOO data to it.*/
    l_master_pipeline_row := get_or_create_pipeline(p_mig_pipeline_history);

    /*
      1. create detail
      2. create ident
      3. create ident data
      4. create detail migration data
      5. create historical huoo data
      */
    BEGIN
      l_length_metre := TO_NUMBER(p_mig_pipeline_history.length) * 1000;
    EXCEPTION WHEN VALUE_ERROR THEN
      ${datasource.user}.migration_logger.log_pipeline(
          p_mig_pipeline_history => p_mig_pipeline_history
        , p_status => 'FAILED'
        , p_message => 'pipeline record length conversion error. length: ' || p_mig_pipeline_history.length
        );
     RAISE;
    END;

    INSERT INTO ${datasource.user}.pipeline_details ( id
                                        , pipeline_id
                                        , pwa_consent_id
                                        , start_timestamp
                                        , end_timestamp
                                        , tip_flag
                                        , pipeline_status
                                        , detail_status
                                        , pipeline_number
                                        , created_by_wua_id
                                        , from_location
                                        , to_location
                                        , length
                                        , products_to_be_conveyed
                                        , trenched_buried_filled_flag
                                        , max_external_diameter
                                        , bundle_name
                                        , pipeline_in_bundle
                                        )
    VALUES ( p_mig_pipeline_history.pd_id
           , l_master_pipeline_row.id
           , p_mig_pipeline_history.pipe_auth_detail_id
           , p_mig_pipeline_history.start_date
           , p_mig_pipeline_history.end_date
           , CASE WHEN p_mig_pipeline_history.status_control = 'C' THEN 1 ELSE NULL END
           , p_mig_pipeline_history.pipeline_status
           , p_mig_pipeline_history.status
           , p_mig_pipeline_history.pipeline_number
           , p_mig_pipeline_history.created_by_wua_id

           , p_mig_pipeline_history.position_from
           , p_mig_pipeline_history.position_to
           , l_length_metre
           , (  -- agreed we can use imperfect existing data as free-text values at the pipeline header level
               SELECT xem.key
               FROM envmgr.xview_env_mapsets xem
               WHERE xem.ms_domain ='PIPELINE_PROD_CODE'
               AND xem.data = p_mig_pipeline_history.product_code
             )
           ,  CASE
                WHEN p_mig_pipeline_history.trenched_y_n = 'Y' THEN 1
                WHEN p_mig_pipeline_history.trenched_y_n = 'N' THEN 0
                -- explicit mapping to null so we can blow up on unexpected values
                WHEN p_mig_pipeline_history.trenched_y_n = 'U' THEN NULL
                WHEN p_mig_pipeline_history.trenched_y_n IS NULL THEN NULL
                ELSE 2 -- check constraint prevents insert in this case
              END
           , p_mig_pipeline_history.diameter
           , CASE
               WHEN INSTR(p_mig_pipeline_history.pipeline_number, '.') != 0
                 THEN SUBSTR(p_mig_pipeline_history.pipeline_number, 0, INSTR(p_mig_pipeline_history.pipeline_number, '.')-1) || ' bundle'
               ELSE NULL
             END
           , CASE
               WHEN INSTR(p_mig_pipeline_history.pipeline_number, '.') != 0 THEN 1
               ELSE 0
             END
           )
    RETURNING id INTO l_detail_id;

    -- identity column
    INSERT INTO ${datasource.user}.pipeline_detail_idents ( pipeline_detail_id
                                              , ident_no
                                              , from_location
                                              , to_location
                                              , length)
    VALUES ( l_detail_id
            , 1 -- default ident number
            -- repeat pipeline header info so it can corrected "in app"
            , p_mig_pipeline_history.position_from
            , p_mig_pipeline_history.position_to
            , l_length_metre)
    RETURNING id INTO l_detail_ident_id;

    INSERT INTO ${datasource.user}.pipeline_detail_ident_data ( pipeline_detail_ident_id
                                              , external_diameter
                                              , wall_thickness
                                              , maop)
    VALUES ( l_detail_ident_id
           , p_mig_pipeline_history.diameter
           , p_mig_pipeline_history.wall_thickness_mm
           , p_mig_pipeline_history.maop)
    RETURNING id INTO l_detail_ident_data_id;

    INSERT INTO ${datasource.user}.pipeline_detail_migration_data ( pipeline_detail_id
                                                      , brown_book_pipeline_type
                                                      , commissioned_date
                                                      , abandoned_date
                                                      , file_reference
                                                      , pipe_material
                                                      , material_grade
                                                      , trench_depth
                                                      , system_identifier
                                                      , psig
                                                      , notes)
    VALUES ( l_detail_id
           , p_mig_pipeline_history.type
           , p_mig_pipeline_history.commissioned_date
           , p_mig_pipeline_history.abandoned_date
           , p_mig_pipeline_history.file_reference
           , p_mig_pipeline_history.pipe_material
           , p_mig_pipeline_history.material_grade
           , p_mig_pipeline_history.trench_depth
           , p_mig_pipeline_history.system_identifier
           , p_mig_pipeline_history.psig
           , p_mig_pipeline_history.notes)
    RETURNING id INTO l_detail_migration_data_id;

    FOR company_hist IN (
      SELECT *
      FROM ${datasource.user}.mig_pipeline_hist_org_roles mphor
      WHERE mphor.pd_id = l_detail_id
      )
    LOOP
        -- batch insert might be quicker, but dont think we need to worry for now.
        INSERT INTO ${datasource.user}.pipeline_detail_migr_huoo_data (pipeline_detail_id
                                                          , organisation_role
                                                          , organisation_unit_id
                                                          , manual_organisation_name)
        VALUES ( company_hist.pd_id
               , company_hist.role
               , company_hist.org_ou_id
               , company_hist.org_manual_name
       );
        l_huoo_role_count := l_huoo_role_count + 1;
    END LOOP;

    COMMIT;

    ${datasource.user}.migration_logger.log_pipeline(
        p_mig_pipeline_history => p_mig_pipeline_history
      , p_status => 'COMPLETE'
      , p_message => ' pipeline record migration FINISHED ' ||
                     ' pwa_id: ' || l_master_pipeline_row.pwa_id ||
                     ' pipeline_id: ' || l_master_pipeline_row.id ||
                     ' pipeline_detail_id: ' || l_detail_id ||
                     ' pipeline_detail_ident_id: ' || l_detail_ident_id ||
                     ' pipeline_detail_ident_data_id: ' || l_detail_ident_data_id ||
                     ' pipeline_detail_migration_data_id: ' || l_detail_migration_data_id ||
                     ' huoo role count: ' || l_huoo_role_count
      );
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      ${datasource.user}.migration_logger.log_pipeline(
          p_mig_pipeline_history => p_mig_pipeline_history
        , p_status => 'FAILED'
        , p_message => SQLERRM || CHR(10) || dbms_utility.format_error_backtrace()
        );

  END migrate_pipeline_history;


  PROCEDURE pwa_huoo_check(p_mig_master_pwa ${datasource.user}.mig_master_pwas%ROWTYPE)
  AS
    l_count_valid_holders NUMBER;
    l_count_invalid_holders NUMBER;
  BEGIN
    SELECT COUNT(*)
    INTO l_count_valid_holders
    FROM ${datasource.user}.mig_pipeline_hist_org_roles mphor
    JOIN ${datasource.user}.mig_pipeline_history mph ON mphor.pd_id = mph.pd_id
    WHERE mphor.status_control = 'C'
    AND mphor.pipeline_status != 'DELETED'
    AND mph.pipe_auth_detail_id = p_mig_master_pwa.pad_id
    AND mphor.org_ou_id IS NOT NULL
    AND mphor.role = 'HOLDER';

    SELECT COUNT(*)
    INTO l_count_invalid_holders
    FROM ${datasource.user}.mig_pipeline_hist_org_roles mphor
    WHERE mphor.status_control = 'C'
    AND mphor.pipeline_status != 'DELETED'
    AND mphor.pipe_auth_detail_id = p_mig_master_pwa.pad_id
    AND org_manual_name IS NOT NULL
    AND mphor.role = 'HOLDER';

    IF(l_count_valid_holders = 0) THEN
      RAISE_APPLICATION_ERROR(-20789, 'Cannot migrate consent where zero valid holder exist!');
    END IF;

    -- hard error for now to highlight required data fixes
    IF(l_count_invalid_holders > 0) THEN
      RAISE_APPLICATION_ERROR(-20789, 'Cannot migrate consent where ' || l_count_invalid_holders || ' invalid holders exist!');
    END IF;

  END pwa_huoo_check;


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
    WHERE mpa.id = l_mig_auth_id;

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
    * 3. Add current HUOO data for latest consent only
        - Dry run migration of top level pipeline to check that we can identify HOLDER, USER, OPERATOR, DATA
          for every pipeline in the top level pwa.
          If this is not possible, rollback the consent migration so data can investigated and fixed up.
    * 4. DEFFERRED to pipeline migration step
        - when top level pipelines are created, at that point link pipeline to huoo data for latest consent on PWA.
    */

    l_master_mig_pwa_consent := get_mig_pwa_consent(p_mig_master_pwa.pad_id);
    ${datasource.user}.migration_logger.log(p_mig_master_pwa, 'STARTING',
        'Starting migration. Initial PWA reference: ' || l_master_mig_pwa_consent.reference);
    previous_migration_check(p_mig_master_pwa);
    pwa_huoo_check(p_mig_master_pwa);

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

    DECLARE
      -- get the last consent on the pwa, as we are dealing only with tip data, doesnt really matter for our purposes if it was a deposit consent,
      -- because the data can only tell us the state of the world at the time of the last consent.
      l_last_consented_consent_id NUMBER := get_last_consented_consent_id(l_master_pwa_detail.pwa_id);
      -- temp variable to ensure we can dry run pwa pipeline migration
      l_master_pipeline_row ${datasource.user}.pipelines%ROWTYPE;
    BEGIN
      -- because of sanity check before migration
      -- only non HOLDER roles might have a manual org name. This will never be entered as new going forward, only for reference
      -- ignore deleted pipelines, how can they have legitimate HUOO info in the new system, and what use would bad data be?
      FOR distinct_huoo_role IN (
        SELECT DISTINCT mphor.role, mphor.org_ou_id, mphor.org_manual_name
        FROM ${datasource.user}.mig_pipeline_hist_org_roles mphor
        WHERE mphor.status_control = 'C'
        AND mphor.pipeline_status != 'DELETED'
        -- all current pipelines attached to master pwa
        AND mphor.pa_id = p_mig_master_pwa.pa_id
      ) LOOP
        INSERT INTO ${datasource.user}.pwa_consent_organisation_roles ( added_by_pwa_consent_id
                                                                      , role
                                                                      , type
                                                                      , ou_id
                                                                      , migrated_organisation_name
                                                                      , start_timestamp)
        VALUES ( l_last_consented_consent_id
               , distinct_huoo_role.role
               , 'PORTAL_ORG'
               , distinct_huoo_role.org_ou_id
               , distinct_huoo_role.org_manual_name
               , SYSTIMESTAMP
               );

      END LOOP;

      -- dry run huoo migration for every pwa pipeline. blow up if any cannot link huoo details exactly as expected.
      FOR dry_run_current_pipeline IN (
        SELECT mph.*
        FROM ${datasource.user}.mig_pipeline_history mph
        JOIN ${datasource.user}.mig_pwa_consents mpc ON mph.pipe_auth_detail_id = mpc.pad_id
        WHERE mph.status_control = 'C'
        AND mph.pipeline_status != 'DELETED'
        AND mpc.pa_id = p_mig_master_pwa.pa_id
      ) LOOP
        -- this will blow up if every current pipeline for the PWA cannot reconcile HUOO data.
        l_master_pipeline_row := get_or_create_pipeline(
         p_mig_pipeline_history => dry_run_current_pipeline
         , p_dry_run => TRUE
         );
      END LOOP;

    END;

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

