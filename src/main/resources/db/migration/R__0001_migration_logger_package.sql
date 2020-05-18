CREATE OR REPLACE PACKAGE ${datasource.user}.migration_logger AS

   FUNCTION updated_log_message(p_old_status VARCHAR2
                              , p_new_status VARCHAR2
                              , p_message VARCHAR2
                              , p_clob_log CLOB)
    RETURN CLOB;

  FUNCTION formatted_log_prefix(p_old_status VARCHAR2
                               , p_new_status VARCHAR2)
    RETURN VARCHAR2;

   PROCEDURE log(p_mig_master_pwa ${datasource.user}.mig_master_pwas%ROWTYPE
                , p_status VARCHAR2
                , p_message VARCHAR2);

END migration_logger;
/

CREATE OR REPLACE PACKAGE BODY ${datasource.user}.migration_logger
AS
  FUNCTION formatted_log_prefix(p_old_status VARCHAR2
                               , p_new_status VARCHAR2)
    RETURN VARCHAR2 AS
  BEGIN
    -- microseconds to 4 decimal places
    RETURN TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SSXFF4') || '; ' || p_old_status || ' -> ' || p_new_status || '; ';
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


END migration_logger;
/

