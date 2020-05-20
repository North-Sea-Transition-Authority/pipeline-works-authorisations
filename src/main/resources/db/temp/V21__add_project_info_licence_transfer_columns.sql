ALTER TABLE ${datasource.user}.pad_project_information ADD (
  licence_transfer_planned INTEGER
, licence_transfer_timestamp TIMESTAMP
, commercial_agreement_timestamp TIMESTAMP
, CONSTRAINT pad_pj_licence_transfer_ck CHECK (licence_transfer_planned IN (0, 1) OR licence_transfer_planned IS NULL)
);
