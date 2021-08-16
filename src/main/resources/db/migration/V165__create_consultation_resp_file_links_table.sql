CREATE TABLE ${datasource.user}.consultation_resp_file_links (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    consultation_resp_id NUMBER NOT NULL,
    af_id NUMBER NOT NULL,
    CONSTRAINT crfl_cr_id_fk FOREIGN KEY (consultation_resp_id) REFERENCES ${datasource.user}.consultation_responses (id),
    CONSTRAINT crfl_af_id_fk FOREIGN KEY (af_id) REFERENCES ${datasource.user}.app_files (id)
);

CREATE INDEX ${datasource.user}.cons_resp_file_link_cr_id_idx ON ${datasource.user}.consultation_resp_file_links (consultation_resp_id);

CREATE INDEX ${datasource.user}.cons_resp_file_link_af_id_idx ON ${datasource.user}.consultation_resp_file_links (af_id);