CREATE TABLE ${datasource.user}.PAD_PIPELINE_TRANSFERS (
    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , PAD_PIPELINE_ID NUMBER NOT NULL
    , DONOR_PAD_ID NUMBER NOT NULL
    , RECIPIENT_PAD_ID NUMBER
    , CREATED_TIMESTAMP TIMESTAMP NOT NULL
    , CONSTRAINT padpipe_id_fk FOREIGN KEY (PAD_PIPELINE_ID) REFERENCES ${datasource.user}.PAD_PIPELINES (ID)
    , CONSTRAINT padproject_donor_fk FOREIGN KEY (DONOR_PAD_ID) REFERENCES ${datasource.user}.PWA_APPLICATION_DETAILS (ID)
    , CONSTRAINT padproject_recipient_fk FOREIGN KEY (RECIPIENT_PAD_ID) REFERENCES ${datasource.user}.PWA_APPLICATION_DETAILS (ID)
);

CREATE INDEX ${datasource.user}.padtransfer_pipeline_id ON ${datasource.user}.PAD_PIPELINE_TRANSFERS(PAD_PIPELINE_ID);
CREATE INDEX ${datasource.user}.padtransfer_pipeline_donor_id ON ${datasource.user}.PAD_PIPELINE_TRANSFERS(DONOR_PAD_ID);
CREATE INDEX ${datasource.user}.padtransfer_pipeline_recipient_id ON ${datasource.user}.PAD_PIPELINE_TRANSFERS(RECIPIENT_PAD_ID);
