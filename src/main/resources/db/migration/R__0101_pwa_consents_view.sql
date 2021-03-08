CREATE OR REPLACE VIEW ${datasource.user}.vw_pwa_consents AS
SELECT
  p.id || pc.reference row_id
, p.id pwa_id
, pc.reference consent_reference
FROM ${datasource.user}.pwas p
JOIN ${datasource.user}.pwa_consents pc ON pc.pwa_id = p.id;