CREATE TABLE ${datasource.user}.PAD_PIPELINE_TRANSFERS (
    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
    , DONOR_PIPELINE_ID NUMBER NOT NULL
    , RECIPIENT_PIPELINE_ID NUMBER
    , DONOR_PAD_ID NUMBER NOT NULL
    , RECIPIENT_PAD_ID NUMBER
    , CREATED_TIMESTAMP TIMESTAMP NOT NULL
    , CONSTRAINT padpipe_donor_id_fk FOREIGN KEY (DONOR_PIPELINE_ID) REFERENCES ${datasource.user}.PIPELINES (ID)
    , CONSTRAINT padpipe_recipient_id_fk FOREIGN KEY (RECIPIENT_PIPELINE_ID) REFERENCES ${datasource.user}.PIPELINES (ID)
    , CONSTRAINT padproject_donor_fk FOREIGN KEY (DONOR_PAD_ID) REFERENCES ${datasource.user}.PWA_APPLICATION_DETAILS (ID)
    , CONSTRAINT padproject_recipient_fk FOREIGN KEY (RECIPIENT_PAD_ID) REFERENCES ${datasource.user}.PWA_APPLICATION_DETAILS (ID)
);

CREATE INDEX ${datasource.user}.padtransfer_donor_pipeline_id ON ${datasource.user}.PAD_PIPELINE_TRANSFERS(DONOR_PIPELINE_ID);
CREATE INDEX ${datasource.user}.padtransfer_recipient_pipeline_id ON ${datasource.user}.PAD_PIPELINE_TRANSFERS(RECIPIENT_PIPELINE_ID);
CREATE INDEX ${datasource.user}.padtransfer_pipeline_donor_id ON ${datasource.user}.PAD_PIPELINE_TRANSFERS(DONOR_PAD_ID);
CREATE INDEX ${datasource.user}.padtransfer_pipeline_recipient_id ON ${datasource.user}.PAD_PIPELINE_TRANSFERS(RECIPIENT_PAD_ID);
