CREATE SEQUENCE ${datasource.user}.pwa_consent_ref_seq
START WITH ${consent-ref.seq-start}
INCREMENT BY 1
NOCACHE;

ALTER TABLE ${datasource.user}.pwa_consents
ADD CONSTRAINT pwa_consent_uniq_ref UNIQUE (reference);