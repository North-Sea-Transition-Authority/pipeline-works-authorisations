CREATE TABLE ${datasource.user}.public_notice_cover_letters (
    id INTEGER PRIMARY KEY
    , text_type VARCHAR2(4000) NOT NULL
    , text CLOB NOT NULL
    , end_timestamp TIMESTAMP
);

CREATE TABLE ${datasource.user}.public_notices (
    id INTEGER PRIMARY KEY
    , application_id INTEGER NOT NULL
    , status VARCHAR2(4000) NOT NULL
    , cover_letter_id INTEGER NOT NULL
    , version INTEGER NOT NULL
    , publication_start_timestamp TIMESTAMP
    , publication_end_timestamp TIMESTAMP
    , submitted_timestamp TIMESTAMP NOT NULL

    , CONSTRAINT pn_pwa_app_fk FOREIGN KEY(application_id) REFERENCES ${datasource.user}.pwa_applications(id)
    , CONSTRAINT pn_cover_letter_fk FOREIGN KEY(cover_letter_id) REFERENCES ${datasource.user}.public_notice_cover_letters(id)
);
CREATE INDEX ${datasource.user}.pn_pwa_app_fk_idx ON ${datasource.user}.public_notices(application_id);
CREATE INDEX ${datasource.user}.pn_cover_letter_fk_idx ON ${datasource.user}.public_notices(cover_letter_id);


CREATE TABLE ${datasource.user}.public_notice_requests (
    id INTEGER PRIMARY KEY
    , public_notice_id INTEGER NOT NULL
    , status VARCHAR2(4000) NOT NULL
    , reason VARCHAR2(4000) NOT NULL
    , reason_description VARCHAR2(4000)
    , version INTEGER NOT NULL
    , submitted_timestamp TIMESTAMP NOT NULL

    , CONSTRAINT pnrequests_pn_fk FOREIGN KEY(public_notice_id) REFERENCES ${datasource.user}.public_notices(id)
);
CREATE INDEX ${datasource.user}.pnrequests_pn_fk_idx ON ${datasource.user}.public_notice_requests(public_notice_id);