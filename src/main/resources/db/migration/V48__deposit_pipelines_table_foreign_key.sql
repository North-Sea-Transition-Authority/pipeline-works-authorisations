ALTER TABLE ${datasource.user}.pad_deposit_pipelines ADD (
    CONSTRAINT pad_permanent_deposit_id_fk FOREIGN KEY(permanent_deposit_info_id) REFERENCES ${datasource.user}.pad_permanent_deposits(id)
);