CREATE OR REPLACE PROCEDURE ${datasource.user}.addHistoricalTerms
( consent_reference VARCHAR2,
  variation_term NUMBER,
  hterm1 NUMBER,
  hterm2 NUMBER,
  hterm3 NUMBER,
  depcon_paragraph NUMBER,
  depcon_schedule NUMBER )

    IS
    PWA_ID NUMBER;

    cursor PWA_CURSOR is
        SELECT cs.pwa_id
        FROM  ${datasource.user}.vw_consent_search cs
        WHERE cs.reference = consent_reference;

BEGIN
    open PWA_CURSOR;
    fetch PWA_CURSOR into PWA_ID;

    IF PWA_ID IS NOT NULL THEN
        INSERT INTO ${datasource.user}.terms_and_conditions (
            pwa_id,
            variation_term,
            huoo_term_one,
            huoo_term_two,
            huoo_term_three,
            depcon_paragraph,
            depcon_schedule,
            created_by,
            created_timestamp)
        VALUES (
                pwa_id,
                variation_term,
                hterm1,
                hterm2,
                hterm3,
                depcon_paragraph,
                depcon_schedule,
                1,
                SYSTIMESTAMP);
        DBMS_OUTPUT.put_line('Record Added');
    ELSE
        DBMS_OUTPUT.put_line('PWA_ID could not be found');
        logger.LOG (CONCAT('PWA_ID could not be found for: ', consent_reference), 10);
    END IF;
    COMMIT;
    close PWA_CURSOR;

EXCEPTION
    WHEN no_data_found THEN
        DBMS_OUTPUT.put_line('Exception caught: ');
        DBMS_OUTPUT.put_line('PWA_ID could not be found');
        logger.LOG (CONCAT('PWA_ID could not be found for: ', consent_reference), 10);
    WHEN too_many_rows THEN
        DBMS_OUTPUT.put_line('Exception caught: ');
        DBMS_OUTPUT.put_line('Multiple PWA_IDs were found');
        logger.LOG (CONCAT('Multiple PWA_IDs were found for: ', consent_reference), 10);
    WHEN OTHERS THEN
        raise_application_error(-20001,'An error was encountered - '||SQLCODE||' -ERROR- '||SQLERRM);
END;
