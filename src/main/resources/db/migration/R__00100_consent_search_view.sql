CREATE OR REPLACE VIEW ${datasource.user}.vw_consent_search AS
WITH consents_fragment AS (
  SELECT pc.pwa_id, MAX(pc.variation_number) max_variation_number
  FROM ${datasource.user}.pwa_consents pc
  GROUP BY pc.pwa_id
)
SELECT
  pd.pwa_id
, pd.reference
, COALESCE(df.field_name, pdf.manual_field_name) field_or_other_ref
, LISTAGG(pog.name, ', ') WITHIN GROUP (ORDER BY 1) holder_names_csv
, initial_consent.consent_timestamp first_consent_timestamp
, latest_consent.reference latest_consent_ref
, latest_consent.consent_timestamp latest_consent_timestamp
FROM ${datasource.user}.pwa_details pd
LEFT JOIN ${datasource.user}.pwa_detail_fields pdf ON pdf.pwa_detail_id = pd.id
LEFT JOIN ${datasource.user}.devuk_fields df ON df.field_id = pdf.devuk_field_id
JOIN ${datasource.user}.pwa_consents pc ON pc.pwa_id = pd.pwa_id
JOIN ${datasource.user}.pwa_consent_organisation_roles pcor ON pcor.added_by_pwa_consent_id = pc.id AND pcor.role = 'HOLDER' AND pcor.end_timestamp IS NULL
JOIN ${datasource.user}.portal_organisation_units pou ON pou.ou_id = pcor.ou_id
JOIN ${datasource.user}.portal_organisation_groups pog ON pog.org_grp_id = pou.org_grp_id
JOIN ${datasource.user}.pwa_consents initial_consent ON initial_consent.pwa_id = pd.pwa_id AND initial_consent.variation_number = 0 AND initial_consent.consent_type = 'INITIAL_PWA'
JOIN consents_fragment cf ON cf.pwa_id = pd.pwa_id
JOIN ${datasource.user}.pwa_consents latest_consent ON latest_consent.pwa_id = cf.pwa_id AND latest_consent.variation_number = cf.max_variation_number
WHERE pd.end_timestamp IS NULL
GROUP BY
  pd.pwa_id
, pd.reference
, COALESCE(df.field_name, pdf.manual_field_name)
, initial_consent.consent_timestamp
, latest_consent.reference
, latest_consent.consent_timestamp
ORDER BY pwa_id DESC