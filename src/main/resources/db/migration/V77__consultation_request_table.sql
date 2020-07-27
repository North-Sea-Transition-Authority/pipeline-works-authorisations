CREATE TABLE ${datasource.user}.consultation_requests (
   id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , application_id NUMBER NOT NULL

    , consultee_group_id NUMBER
    , other_group_selected INTEGER CHECK(other_group_selected IN (0, 1))
    , other_group_login VARCHAR2(4000)

    , deadline_date TIMESTAMP
    , status VARCHAR2(4000)
    , start_timestamp TIMESTAMP
    , started_by_person_id NUMBER NOT NULL
    , end_timestamp TIMESTAMP
    , ended_by_person_id NUMBER
    , ended_reason VARCHAR2(4000)


    , CONSTRAINT cons_req_pad_fk FOREIGN KEY(application_id) REFERENCES ${datasource.user}.pwa_applications(id)
    , CONSTRAINT cons_req_cons_group_fk FOREIGN KEY(consultee_group_id) REFERENCES ${datasource.user}.consultee_groups(id)
);

CREATE INDEX ${datasource.user}.cons_req_pad_fk_idx ON ${datasource.user}.consultation_requests(application_id);
CREATE INDEX ${datasource.user}.cons_req_cons_group_fk_idx ON ${datasource.user}.consultation_requests(consultee_group_id);
CREATE INDEX ${datasource.user}.cons_req_start_person_id_idx ON ${datasource.user}.consultation_requests(started_by_person_id);
CREATE INDEX ${datasource.user}.cons_req_end_person_id_idx ON ${datasource.user}.consultation_requests(ended_by_person_id)