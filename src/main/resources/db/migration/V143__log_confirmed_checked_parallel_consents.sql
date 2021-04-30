CREATE TABLE ${datasource.user}.parallel_consent_check_log (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
, pad_consent_review_id NUMBER NOT NULL
, CONSTRAINT pccl_consent_review_fk FOREIGN KEY (pad_consent_review_id) REFERENCES ${datasource.user}.pad_consent_reviews (id)
, pwa_consent_id NUMBER NOT NULL
, CONSTRAINT pccl_pwa_consent_fk FOREIGN KEY (pwa_consent_id) REFERENCES ${datasource.user}.pwa_consents (id)
, checked_by_person_id NUMBER NOT NULL
, check_confirmed_timestamp TIMESTAMP NOT NULL
);

