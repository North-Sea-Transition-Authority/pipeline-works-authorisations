GRANT SELECT ON decmgr.pipeline_authorisations TO ${datasource.user};

GRANT SELECT ON decmgr.xview_pipeline_auth_details TO ${datasource.user};

GRANT SELECT ON decmgr.pipeline_authorisation_details TO ${datasource.user};

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
  WHERE xpad.variation_number = 0
  AND xpad.consent_date IS NOT NULL
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


