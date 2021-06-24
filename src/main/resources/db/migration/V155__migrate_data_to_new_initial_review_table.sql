CREATE TABLE ${datasource.user}.pad_initial_review (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , application_detail_id INTEGER NOT NULL
    , init_review_approved_timestamp TIMESTAMP NOT NULL
    , init_review_approved_by_wua_id INTEGER NOT NULL
    , approval_revoked_timestamp TIMESTAMP
    , approval_revoked_by_wua_id INTEGER

    , CONSTRAINT padir_pad_fk FOREIGN KEY(application_detail_id) REFERENCES ${datasource.user}.pwa_application_details(id)
);
CREATE INDEX ${datasource.user}.padir_pad_fk_idx ON ${datasource.user}.pad_initial_review(application_detail_id);


--migrate existing initial review columns from app detail table to initial review table
INSERT INTO ${datasource.user}.pad_initial_review (application_detail_id,
                                                   init_review_approved_timestamp,
                                                   init_review_approved_by_wua_id)

    SELECT pad.id, pad.init_review_approved_timestamp, pad.init_review_approved_by_wua_id
    FROM ${datasource.user}.pwa_application_details pad
    WHERE pad.init_review_approved_timestamp IS NOT NULL;



--delete initial review columns from app detail table
ALTER TABLE ${datasource.user}.pwa_application_details DROP COLUMN init_review_approved_timestamp;
ALTER TABLE ${datasource.user}.pwa_application_details DROP COLUMN init_review_approved_by_wua_id;