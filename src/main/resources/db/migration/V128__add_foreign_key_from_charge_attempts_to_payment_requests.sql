ALTER TABLE ${datasource.user}.pwa_app_charge_payment_attempt ADD (
  pwa_payment_request_uuid CHAR(36) NOT NULL
, CONSTRAINT pacpa_ppr_id_fk FOREIGN KEY (pwa_payment_request_uuid) REFERENCES ${datasource.user}.pwa_payment_requests(uuid)
);

CREATE INDEX ${datasource.user}.pacpa_ppr_id_fk_idx ON ${datasource.user}.pwa_app_charge_payment_attempt(pwa_payment_request_uuid);