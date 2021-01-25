CREATE OR REPLACE VIEW ${datasource.user}.api_vw_pwa_pipeline_details AS
SELECT
  pd.id pipeline_detail_id
, pd.pipeline_id
, pd.pwa_consent_id
, CAST(pd.start_timestamp AS DATE) start_date
, CAST(pd.end_timestamp AS DATE) end_date
, CASE WHEN pd.tip_flag = 1 THEN 'C' END status_control
, pd.pipeline_number
, pd.pipeline_status -- PENDING, DELETED, NEVER_LAID can all be ignored gnenerally
, pd.from_location
, pd.to_location
, pd.max_external_diameter
, COALESCE(pdmd.commissioned_date, NULL) commissioned_date -- we do not map commissioned date in new system
FROM ${datasource.user}.pipeline_details pd
LEFT JOIN ${datasource.user}.pipeline_detail_migration_data pdmd on pdmd.pipeline_detail_id = pd.id;


CREATE OR REPLACE VIEW ${datasource.user}.api_vw_pwa_consents AS
SELECT
  pc.id pwa_consent_id
, pc.reference pwa_consent_reference
, CAST(pc.consent_timestamp AS DATE) consented_date
, pc.pwa_id primary_pwa_id
, pc.consent_type
FROM ${datasource.user}.pwa_consents pc;


CREATE OR REPLACE VIEW ${datasource.user}.api_vw_primary_pwas AS
SELECT
  pwad.reference primary_pwa_reference
, pwad.pwa_id primary_pwa_id
, pc.reference initial_pwa_consent_reference
, CAST(pc.consent_timestamp AS DATE) initial_pwa_consent_date
, pc.id initial_pwa_consent_id
FROM ${datasource.user}.pwa_details pwad
LEFT JOIN ${datasource.user}.pwa_consents pc ON pwad.pwa_id = pc.pwa_id
WHERE pwad.end_timestamp IS NULL
AND pwad.pwa_status = 'CONSENTED'
-- below condition should remove cardinality as only ever 1 initial consent per PWA.
AND pc.consent_type = 'INITIAL_PWA';

GRANT SELECT ON ${datasource.user}.api_vw_pwa_pipeline_details TO appenv;
GRANT SELECT ON ${datasource.user}.api_vw_pwa_consents TO appenv;
GRANT SELECT ON ${datasource.user}.api_vw_primary_pwas TO appenv;