CREATE TABLE ${datasource.user}.pad_consent_reviews (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pad_id NUMBER NOT NULL
, status VARCHAR2(25) NOT NULL
, cover_letter_text CLOB NOT NULL
, started_by_person_id NUMBER NOT NULL
, start_timestamp TIMESTAMP NOT NULL
, ended_by_person_id NUMBER
, end_timestamp TIMESTAMP
, ended_reason VARCHAR2(4000)
, CONSTRAINT pad_id_fk FOREIGN KEY (pad_id) REFERENCES ${datasource.user}.pwa_application_details (id)
);

CREATE INDEX ${datasource.user}.pad_consent_reviews_pad_id_idx ON ${datasource.user}.pad_consent_reviews (pad_id);

CREATE INDEX ${datasource.user}.pad_consent_reviews_starts_idx ON ${datasource.user}.pad_consent_reviews (start_timestamp);