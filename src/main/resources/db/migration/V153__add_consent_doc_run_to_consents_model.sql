ALTER TABLE ${datasource.user}.pwa_consents ADD docgen_run_id NUMBER;

ALTER TABLE ${datasource.user}.pwa_consents ADD CONSTRAINT docgen_run_id_fk FOREIGN KEY (docgen_run_id)
REFERENCES ${datasource.user}.docgen_runs (id);

CREATE INDEX ${datasource.user}.pwa_consents_docgen_run_id_idx ON ${datasource.user}.pwa_consents (docgen_run_id);