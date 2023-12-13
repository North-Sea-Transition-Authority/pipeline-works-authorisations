INSERT INTO ${datasource.user}.DOCUMENT_TEMPLATES (Mnem) VALUES ('HYDROGEN_CONSENT_DOCUMENT');
INSERT INTO
    ${datasource.user}.DT_SECTIONS (DT_ID, NAME, STATUS, START_TIMESTAMP)
    (SELECT
         (SELECT ID FROM ${datasource.user}.DOCUMENT_TEMPLATES WHERE Mnem = 'HYDROGEN_CONSENT_DOCUMENT') ,
         NAME,
         STATUS,
         CURRENT_TIMESTAMP
     FROM
         ${datasource.user}.DT_SECTIONS
     WHERE
             DT_ID = (SELECT ID FROM ${datasource.user}.DOCUMENT_TEMPLATES WHERE Mnem = 'PETROLEUM_CONSENT_DOCUMENT'));
CREATE PRIVATE TEMPORARY TABLE ora$ptt_hydrogen_petroleum_mapping (
    hydrogen_section_clause_id NUMBER,
    petroleum_section_clause_id NUMBER
);
DECLARE
    new_id NUMBER;
BEGIN
    FOR record IN (SELECT dtsc.ID, dtsc.S_ID, dts.NAME, dt.mnem
                   FROM ${datasource.user}.DT_SECTION_CLAUSES dtsc
                        JOIN ${datasource.user}.DT_SECTIONS dts ON dts.ID = dtsc.S_ID
                        JOIN ${datasource.user}.DOCUMENT_TEMPLATES dt ON dt.ID = dts.DT_ID
                   WHERE dt.mnem = 'PETROLEUM_CONSENT_DOCUMENT'
                   ORDER BY dtsc.ID ASC)
        LOOP
            SELECT ${datasource.user}.dt_section_clauses_id_seq.NEXTVAL INTO new_id FROM DUAL;
            INSERT INTO ora$ptt_hydrogen_petroleum_mapping VALUES (new_id, record.ID);
            INSERT INTO ${datasource.user}.DT_SECTION_CLAUSES (ID, S_ID)
            SELECT
                new_id,
                dts2.ID
            FROM
                ${datasource.user}.DT_SECTIONS dts2
                JOIN ${datasource.user}.DOCUMENT_TEMPLATES dt2 ON dts2.DT_ID = dt2.ID
            WHERE
                    dts2.NAME = record.name
                    AND dt2.mnem = 'HYDROGEN_CONSENT_DOCUMENT';
            INSERT INTO ${datasource.user}.DT_SECTION_CLAUSE_VERSIONS (
                ID,
                SC_ID,
                VERSION_NO,
                TIP_FLAG,
                NAME,
                TEXT,
                PARENT_SC_ID,
                LEVEL_ORDER,
                STATUS,
                CREATED_TIMESTAMP,
                CREATED_BY_PERSON_ID)
            SELECT
                ${datasource.user}.dt_scv_id_seq.NEXTVAL,
                new_id,
                1,
                1,
                dtscv.NAME,
                dtscv.TEXT,
                (SELECT hydrogen_section_clause_id FROM ora$ptt_hydrogen_petroleum_mapping WHERE petroleum_section_clause_id = dtscv.PARENT_SC_ID),
                LEVEL_ORDER,
                STATUS,
                CURRENT_TIMESTAMP,
                1
            FROM
                ${datasource.user}.DT_SECTION_CLAUSE_VERSIONS dtscv
            WHERE
                    dtscv.SC_ID = record.ID
                    AND dtscv.TIP_FLAG = 1;
        END LOOP;
END;
