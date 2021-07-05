
ALTER TABLE ${datasource.user}.pad_pipelines ADD (
    pipeline_transfer_agreed NUMBER
    CONSTRAINT padp_transfer_agreed_ck CHECK(pipeline_transfer_agreed IN (0, 1) OR pipeline_transfer_agreed IS NULL)
);
