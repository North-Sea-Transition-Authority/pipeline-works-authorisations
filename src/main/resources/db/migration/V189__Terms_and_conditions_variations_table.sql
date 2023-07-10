CREATE TABLE ${datasource.user}.terms_and_conditions_variations (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , pwa_id NUMBER
    , variation_term NUMBER
    , huoo_terms VARCHAR2(4000)
    , depcon_paragraph NUMBER
    , depcon_schedule NUMBER
    , created_by NUMBER NOT NULL
    , created_timestamp TIMESTAMP NOT NULL

    , CONSTRAINT tacv_pwa_id_fk FOREIGN KEY (pwa_id) REFERENCES ${datasource.user}.pwas (id)
);

CREATE INDEX ${datasource.user}.tacv_pwa_id_idx ON ${datasource.user}.terms_and_conditions_variations (pwa_id);