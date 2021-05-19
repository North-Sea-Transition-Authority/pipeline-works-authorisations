CREATE OR REPLACE VIEW ${datasource.user}.api_vw_pwa_pipeline_details AS
SELECT *
FROM (
  WITH pipeline_types AS (
  -- flattened version PipelineType
  SELECT 'UNKNOWN' type_mnem, 'Unknown pipeline type' type_display, 'SINGLE_CORE' core_type
  FROM DUAL
  UNION ALL
  SELECT 'PRODUCTION_FLOWLINE' type_mnem, 'Production Flowline' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'PRODUCTION_JUMPER' type_mnem, 'Production Jumper' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'GAS_LIFT_PIPELINE' type_mnem, 'Gas Lift Pipeline' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'GAS_LIFT_JUMPER' type_mnem, 'Gas Lift Jumper' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'WATER_INJECTION_PIPELINE' type_mnem, 'Water Injection Pipeline' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'WATER_INJECTION_JUMPER' type_mnem, 'Water Injection Jumper' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'METHANOL_PIPELINE' type_mnem, 'Methanol Pipeline' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'SERVICES_UMBILICAL' type_mnem, 'Services Umbilical' type_display, 'MULTI_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'HYDRAULIC_JUMPER' type_mnem, 'Hydraulic Jumper' type_display, 'MULTI_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'CHEMICAL_JUMPER' type_mnem, 'Chemical Jumper' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'CONTROL_JUMPER' type_mnem, 'Control Jumper' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'UMBILICAL_JUMPER' type_mnem, 'Umbilical Jumper' type_display, 'MULTI_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'CABLE' type_mnem, 'Cable' type_display, 'SINGLE_CORE' core_type
  FROM dual
  )
  SELECT
    pd.id pipeline_detail_id
  , pd.pipeline_id
  , pd.pwa_consent_id
  , CAST(pd.start_timestamp AS DATE) start_date
  , CAST(pd.end_timestamp AS DATE) end_date
  , CASE WHEN pd.tip_flag = 1 THEN 'C' END status_control
  , pd.pipeline_number
  , pd.pipeline_status -- PENDING, DELETED, NEVER_LAID, TRANSFERRED can all be ignored generally
  , pd.from_location
  , pd.to_location
  , pd.max_external_diameter
  , COALESCE(pdmd.commissioned_date, NULL) commissioned_date -- we do not map commissioned date in new system
  , pd.pipeline_number || ' -' ||
    CASE
      WHEN pt.core_type = 'SINGLE_CORE' AND pd.max_external_diameter IS NOT NULL THEN ' ' || pd.max_external_diameter || ' Millimetre'
    END || ' ' || pt.type_display ||
    CASE WHEN pd.pipeline_in_bundle  = 1 THEN ' (' || pd.bundle_name || ')'
    END pipeline_name
  FROM ${datasource.user}.pipeline_details pd
  LEFT JOIN ${datasource.user}.pipeline_detail_migration_data pdmd ON pdmd.pipeline_detail_id = pd.id
  JOIN pipeline_types pt ON COALESCE(pd.pipeline_type, 'UNKNOWN') = pt.type_mnem
);

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