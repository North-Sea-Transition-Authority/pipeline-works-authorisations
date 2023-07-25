/* This view MUST contain 1 row per pipeline, no duplicates */
CREATE OR REPLACE VIEW ${datasource.user}.api_vw_pipeline_as_built_data AS
SELECT *
FROM (
  SELECT
    pd.pipeline_id
  , abns.date_work_completed date_laid
  , abns.date_pipeline_brought_into_use
  FROM ${datasource.user}.as_built_notif_submissions abns
  JOIN ${datasource.user}.as_built_notif_grp_pipelines abngp ON abngp.id = abns.as_built_notif_pipeline_id
  JOIN ${datasource.user}.pipeline_details pd ON pd.id = abngp.pipeline_detail_id
  JOIN ${datasource.user}.as_built_notification_groups abng ON abngp.as_built_notification_group_id = abng.id
  JOIN ${datasource.user}.pwa_consents pc ON pc.id = abng.pwa_consent_id
  WHERE abns.tip_flag = 1
  AND abngp.pipeline_change_category = 'NEW_PIPELINE' -- assumption here is one and only one 'NEW_PIPELINE' as built change per pipeline over its entire lifetime
);

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
  SELECT 'HYDRAULIC_JUMPER_MULTI_CORE' type_mnem, 'Hydraulic Jumper (multi-core)' type_display, 'MULTI_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'HYDRAULIC_JUMPER_SINGLE_CORE' type_mnem, 'Hydraulic Jumper (single-core)' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'CHEMICAL_JUMPER' type_mnem, 'Chemical Jumper' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'CONTROL_JUMPER_SINGLE_CORE' type_mnem, 'Control Jumper (single-core)' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'CONTROL_JUMPER_MULTI_CORE' type_mnem, 'Control Jumper (multi-core)' type_display, 'MULTI_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'UMBILICAL_JUMPER' type_mnem, 'Umbilical Jumper' type_display, 'MULTI_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'CABLE' type_mnem, 'Cable' type_display, 'SINGLE_CORE' core_type
  FROM dual
  UNION ALL
  SELECT 'POLYMER_INJECTION_PIPELINE' type_mnem, 'Polymer Injection Pipeline' type_display, 'SINGLE_CORE' core_type
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
  , pd.pipeline_type
  , CASE WHEN pd.pipeline_type IS NOT NULL THEN pt.type_display END pipeline_type_display
  , pd.pipeline_status -- PENDING, DELETED, NEVER_LAID, TRANSFERRED can all be ignored generally
  , pd.from_location
  , pd.to_location
  , pd.max_external_diameter
  , pd.length
  , pd.pipeline_number || ' -' ||
    CASE
      WHEN pt.core_type = 'SINGLE_CORE' AND pd.max_external_diameter IS NOT NULL THEN ' ' || pd.max_external_diameter || ' Millimetre'
    END || ' ' || pt.type_display ||
    CASE WHEN pd.pipeline_in_bundle  = 1 THEN ' (' || pd.bundle_name || ')'
    END pipeline_name
  , pd.products_to_be_conveyed
  , pdmd.brown_book_pipeline_type migrated_pipeline_type
  , pdmd.abandoned_date
  FROM ${datasource.user}.pipeline_details pd
  LEFT JOIN ${datasource.user}.pipeline_detail_migration_data pdmd ON pdmd.pipeline_detail_id = pd.id
  JOIN pipeline_types pt ON COALESCE(pd.pipeline_type, 'UNKNOWN') = pt.type_mnem
  WHERE pd.pipeline_status NOT IN ('DELETED', 'PENDING')
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

CREATE OR REPLACE VIEW ${datasource.user}.api_vw_current_pipeline_data AS
SELECT *
FROM (
  SELECT
    ppd.pipeline_id
  , ppd.pipeline_number
  , ppd.pipeline_name
  , ppd.from_location
  , ppd.to_location
  , ppd.max_external_diameter
  , ppd.pwa_consent_id pipeline_linked_pwa_consent_id
  , ppd.pipeline_status
  , ppd.products_to_be_conveyed
  , ppd.migrated_pipeline_type
  , ppd.pipeline_type
  , ppd.pipeline_type_display
  , ppd.length length_metre
  , CASE
      WHEN ppd.length IS NOT NULL THEN ppd.length / 1000
    END length_kilometre
  , ppd.abandoned_date
  , pabd.date_pipeline_brought_into_use
  , pp.primary_pwa_reference
  , pp.primary_pwa_id
  , pp.initial_pwa_consent_id
  , pp.initial_pwa_consent_date
  FROM ${datasource.user}.api_vw_pwa_pipeline_details ppd
  JOIN ${datasource.user}.api_vw_pwa_consents ppc ON ppd.pwa_consent_id = ppc.pwa_consent_id
  JOIN ${datasource.user}.api_vw_primary_pwas pp ON pp.primary_pwa_id = ppc.primary_pwa_id
  LEFT JOIN ${datasource.user}.api_vw_pipeline_as_built_data pabd ON ppd.pipeline_id = pabd.pipeline_id
  WHERE ppd.status_control = 'C'
);

CREATE OR REPLACE VIEW ${datasource.user}.api_vw_current_pipeline_orgs AS
SELECT *
FROM (
  SELECT
    porl.pipeline_id
  , pcor.role huoo_role
  , pcor.type huoo_type
  , pcor.agreement treaty_agreement
  , pcor.ou_id
  , pcor.migrated_organisation_name
  FROM ${datasource.user}.pipeline_org_role_links porl
  JOIN ${datasource.user}.pwa_consent_organisation_roles pcor ON porl.pwa_consent_org_role_id = pcor.id
  WHERE porl.end_timestamp IS NULL
  AND pcor.end_timestamp IS NULL
);

/* Use this view to get basic information about submitted PWA applications. */
CREATE OR REPLACE VIEW ${datasource.user}.api_vw_applications AS
SELECT
  pa.id pwa_application_id
, pa.pwa_id
, pa.app_reference
, ppi.project_name
, pad_lookup.created_timestamp
, pad_lookup.last_submitted_timestamp
, pc.id pwa_consent_id -- the consent created after app completed if available. will be null if app not yet consented.
, pa.applicant_ou_id
FROM ${datasource.user}.pwa_applications pa
JOIN ${datasource.user}.pwa_application_details pad ON pa.id = pad.pwa_application_id
JOIN (
  SELECT
    pad2.pwa_application_id
  , MIN(pad2.created_timestamp) created_timestamp
  , MAX(pad2.submitted_timestamp) last_submitted_timestamp
  FROM ${datasource.user}.pwa_application_details pad2
  GROUP BY pad2.pwa_application_id
) pad_lookup ON pad_lookup.pwa_application_id = pa.id
LEFT JOIN ${datasource.user}.pad_project_information ppi ON pad.id = ppi.application_detail_id
LEFT JOIN ${datasource.user}.pwa_consents pc ON pc.source_pwa_application_id = pa.id
WHERE pad.tip_flag = 1;

/**
  This view gives a snapshot of the current high level HUOOs for each core PWA.
  This view cannot be used to determine the HUOO values for specific consented pipelines, just the current huoo state of the pwa.
  Pipeline HUOOs can be looked up with the api_vw_current_pipeline_orgs.
 */
CREATE OR REPLACE VIEW ${datasource.user}.api_vw_current_pwa_huoos AS
SELECT
  pc.pwa_id
, pcor.ou_id
, pcor.agreement treaty_agreement
, pcor.role
FROM ${datasource.user}.pwa_consent_organisation_roles pcor
JOIN ${datasource.user}.pwa_consents pc ON pcor.added_by_pwa_consent_id = pc.id
WHERE pcor.ended_by_pwa_consent_id IS NULL;


GRANT SELECT ON ${datasource.user}.api_vw_pipeline_as_built_data TO appenv;
GRANT SELECT ON ${datasource.user}.api_vw_pwa_pipeline_details TO appenv;
GRANT SELECT ON ${datasource.user}.api_vw_pwa_consents TO appenv;
GRANT SELECT ON ${datasource.user}.api_vw_primary_pwas TO appenv;
GRANT SELECT ON ${datasource.user}.api_vw_current_pipeline_orgs TO appenv;

GRANT SELECT ON ${datasource.user}.api_vw_current_pipeline_data TO appenv, decmgr, eemsmgr, bpmmgr;

-- PETS needs specific access and ability to build a view
GRANT SELECT ON ${datasource.user}.api_vw_current_pipeline_data TO envmgr WITH GRANT OPTION;

-- UKSS needs specific access and ability to build a view
GRANT SELECT ON ${datasource.user}.api_vw_current_pipeline_orgs TO passmgr WITH GRANT OPTION;
