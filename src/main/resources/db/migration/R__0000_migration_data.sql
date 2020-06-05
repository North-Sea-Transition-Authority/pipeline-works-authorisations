GRANT SELECT ON decmgr.pipeline_authorisations TO ${datasource.user};

GRANT SELECT ON decmgr.xview_pipeline_auth_details TO ${datasource.user};

GRANT SELECT ON decmgr.pipeline_authorisation_details TO ${datasource.user};

GRANT SELECT ON decmgr.xview_pipelines_history TO ${datasource.user};

GRANT SELECT ON decmgr.xview_pipeline_company_hist TO ${datasource.user};

GRANT SELECT ON envmgr.xview_env_mapsets TO ${datasource.user};

CREATE OR REPLACE VIEW ${datasource.user}.mig_master_pwas AS (
  SELECT
    xpad.pad_id
  , xpad.pa_id
  , xpad.papp_id
  , pa.first_pad_id
  , xpad.variation_number
  , xpad.consent_date
  , xpad.reference
  , xpad.pwa_status
  FROM decmgr.xview_pipeline_auth_details xpad
  JOIN decmgr.pipeline_authorisations pa ON xpad.pa_id = pa.id
  WHERE pa.first_pad_id = xpad.pad_id
);


CREATE OR REPLACE VIEW ${datasource.user}.mig_pwa_consents AS (
  SELECT
     xpad.pad_id
   , xpad.pa_id
   , xpad.papp_id
   , pa.first_pad_id
   , xpad.variation_number
   , xpad.consent_date
   , xpad.reference
   , xpad.pwa_status
  FROM decmgr.xview_pipeline_auth_details xpad
  JOIN decmgr.pipeline_authorisations pa ON xpad.pa_id = pa.id
);

CREATE OR REPLACE VIEW ${datasource.user}.mig_pipeline_history AS
SELECT *
FROM decmgr.xview_pipelines_history;


CREATE OR REPLACE VIEW ${datasource.user}.mig_pipeline_hist_org_roles AS
SELECT
  XPH.pd_id
, xph.pipeline_id
, xph.status pipeline_status
, pad.pa_id
, xph.pipe_auth_detail_id
, xph.status_control
, XPCH.NAME org_manual_name
, XPCH.ORGAN_ID org_ou_id
, CASE WHEN XPCH.ROLE = 'OPERAT' THEN 'OPERATOR' ELSE xpch.role END role
FROM decmgr.xview_pipelines_history xph
JOIN decmgr.xview_pipeline_company_hist xpch ON XPH.PD_ID = XPCH.PD_ID
JOIN decmgr.xview_pipeline_auth_details pad ON xph.pipe_auth_detail_id = pad.pad_id
 -- have to exclude rows where the important info is just missing due to dodgy data.
WHERE (xpch.organ_id IS NOT NULL OR xpch.name IS NOT NULL);