ALTER TABLE ${datasource.user}.consultee_group_details
ADD response_document_type VARCHAR2(64);

UPDATE ${datasource.user}.consultee_group_details
SET response_document_type =
    CASE
        WHEN cg_id = 1 THEN 'SECRETARY_OF_STATE_DECISION'
        ELSE 'DEFAULT'
END;

ALTER TABLE ${datasource.user}.consultee_group_details
MODIFY response_document_type VARCHAR2(64) NOT NULL;