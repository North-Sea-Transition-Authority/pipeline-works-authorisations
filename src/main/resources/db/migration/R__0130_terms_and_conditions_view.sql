CREATE OR REPLACE VIEW ${datasource.user}.vw_pwa_terms_and_conditions AS
SELECT p.id pwa_id
, pc.reference consent_reference
FROM ${datasource.user}.pwas p
JOIN ${datasource.user}.pwa_consents pc ON pc.pwa_id = p.id AND pc.consent_type = 'INITIAL_PWA'
LEFT OUTER JOIN ${datasource.user}.terms_and_conditions tc ON tc.pwa_id = p.id
WHERE tc.pwa_id IS NULL;
