CREATE TABLE ${datasource.user}.consultation_responses (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , cr_id NUMBER NOT NULL
    , response_type VARCHAR2(50) NOT NULL
    , response_text VARCHAR2(4000)
    , response_timestamp TIMESTAMP NOT NULL
    , responding_person NUMBER NOT NULL

    , CONSTRAINT cons_resp_consreq_fk FOREIGN KEY(cr_id) REFERENCES ${datasource.user}.consultation_requests(id)
);

CREATE INDEX ${datasource.user}.cons_resp_consreq_fk_idx ON ${datasource.user}.consultation_responses(cr_id);
CREATE INDEX ${datasource.user}.cons_resp_resp_person_idx ON ${datasource.user}.consultation_responses(responding_person)