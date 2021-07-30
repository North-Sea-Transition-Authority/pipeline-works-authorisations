UPDATE ${datasource.user}.consultee_group_details
SET csv_response_option_group_list = 'EIA_REGS,HABITATS_REGS'
WHERE abbreviation = 'EMT';

CREATE TABLE ${datasource.user}.consultation_response_data (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cons_response_id NUMBER NOT NULL,
    response_group VARCHAR2(50) NOT NULL,
    response_type VARCHAR2(50) NOT NULL,
    response_text VARCHAR2(4000),
    CONSTRAINT cons_resp_dat_cons_resp_fk FOREIGN KEY(cons_response_id) REFERENCES ${datasource.user}.consultation_responses(id)
);

CREATE INDEX ${datasource.user}.cons_resp_dat_cons_resp_fk_idx ON ${datasource.user}.consultation_response_data(cons_response_id);

INSERT INTO ${datasource.user}.consultation_response_data (cons_response_id, response_group, response_type, response_text)
SELECT id, 'CONTENT', response_type, response_text
FROM ${datasource.user}.consultation_responses;

UPDATE ${datasource.user}.consultation_response_data
SET response_group = 'ADVICE'
WHERE response_type IN ('PROVIDE_ADVICE', 'NO_ADVICE');

ALTER TABLE ${datasource.user}.consultation_responses DROP COLUMN response_type;
ALTER TABLE ${datasource.user}.consultation_responses DROP COLUMN response_text;