CREATE TABLE ${datasource.user}.template_text (
    id INTEGER PRIMARY KEY
    , text_type VARCHAR2(4000) NOT NULL
    , text CLOB NOT NULL
    , end_timestamp TIMESTAMP
);

CREATE TABLE ${datasource.user}.public_notices (
    id INTEGER PRIMARY KEY
    , application_id INTEGER NOT NULL
    , status VARCHAR2(4000) NOT NULL
    , version INTEGER NOT NULL

    , CONSTRAINT pn_pwa_app_fk FOREIGN KEY(application_id) REFERENCES ${datasource.user}.pwa_applications(id)
);
CREATE INDEX ${datasource.user}.pn_pwa_app_fk_idx ON ${datasource.user}.public_notices(application_id);


CREATE TABLE ${datasource.user}.public_notice_dates (
    id INTEGER PRIMARY KEY
    , public_notice_id INTEGER NOT NULL
    , publication_start_timestamp TIMESTAMP
    , publication_end_timestamp TIMESTAMP
    , created_by_person_id INTEGER NOT NULL
    , ended_by_person_id INTEGER

    , CONSTRAINT pndates_pn_fk FOREIGN KEY(public_notice_id) REFERENCES ${datasource.user}.public_notices(id)
);
CREATE INDEX ${datasource.user}.pndates_pn_fk_idx ON ${datasource.user}.public_notice_dates(public_notice_id);


CREATE TABLE ${datasource.user}.public_notice_requests (
    id INTEGER PRIMARY KEY
    , public_notice_id INTEGER NOT NULL
    , cover_letter_text CLOB NOT NULL
    , status VARCHAR2(4000) NOT NULL
    , reason VARCHAR2(4000) NOT NULL
    , reason_description VARCHAR2(4000)
    , version INTEGER NOT NULL
    , submitted_timestamp TIMESTAMP NOT NULL
    , created_by_person_id INTEGER NOT NULL
    , ended_by_person_id INTEGER

    , CONSTRAINT pnrequests_pn_fk FOREIGN KEY(public_notice_id) REFERENCES ${datasource.user}.public_notices(id)
);
CREATE INDEX ${datasource.user}.pnrequests_pn_fk_idx ON ${datasource.user}.public_notice_requests(public_notice_id);