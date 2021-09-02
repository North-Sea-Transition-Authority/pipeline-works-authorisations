/**
  This query can be used if you want to know the PWA huoo state at the point of a specific consent taking effect.
  Note old consents which have been migrated will have no data as historic HUOO pipeline data couldn't be mapped to specific consents.
  Example use case: core PWA 1/W/50 has 3 consents, 1/W/50, 1/V/51 and 2/V/51. Each of the consents changes some HUOO values on the PWA.
  If you want to know the HUOO state once 1/V/50 was consented, but before the changes in 2/V/50 took effect, you can use this query to find that out by filtering on the pwa_consent_id.
**/
WITH internal_api_vw_consents_rank AS (
SELECT
  pc.id pc_id
, pc.pwa_id
, pc.reference consent_reference
, RANK() OVER (PARTITION BY pc.pwa_id ORDER BY pc.consent_timestamp ASC, pc.id ASC) rank
  FROM ${datasource.user}.pwa_consents pc
), consent_effect_lookup AS (
SELECT
  iavcr1.pwa_id
, iavcr1.pc_id search_consent_pc_id
, iavcr1.consent_reference
, iavcr1.rank search_consent_rank
, iavcr2.pc_id pc_id_impacts_search_consent
, iavcr2.consent_reference consent_impacts_search_consent
FROM internal_api_vw_consents_rank iavcr1
-- consents can obviously be impacted by them themselves. a consent can add or end org roles.
JOIN internal_api_vw_consents_rank iavcr2 ON iavcr1.pwa_id = iavcr2.pwa_id AND iavcr1.rank >= iavcr2.rank
ORDER BY iavcr1.pwa_id, iavcr1.rank, iavcr2.rank
)
SELECT
cel.pwa_id
, cel.search_consent_pc_id pwa_consent_id
, cel.consent_reference
, pcor.added_by_pwa_consent_id role_added_by_pwa_consent_id
, pcor.ended_by_pwa_consent_id role_ended_by_pwa_consent_id
, pcor.role
, pcor.ou_id
, pcor.agreement
, pcor.migrated_organisation_name
FROM consent_effect_lookup cel
LEFT JOIN ${datasource.user}.pwa_consent_organisation_roles pcor ON
  -- include rows where the org role added by the search consent and its impacting PWA consents
  (pcor.added_by_pwa_consent_id = cel.pc_id_impacts_search_consent)
  -- exclude rows where
  AND (
    -- the role has never been ended once added
    pcor.ended_by_pwa_consent_id IS NULL
    -- or the role has been ended and the consent that did the ending is one that impacts (is or occurred before) the search consent
    OR (
      pcor.ended_by_pwa_consent_id IS NOT NULL
      AND pcor.ended_by_pwa_consent_id NOT IN (
        SELECT cel_nested.pc_id_impacts_search_consent
        FROM consent_effect_lookup cel_nested
        WHERE cel.search_consent_pc_id = cel_nested.search_consent_pc_id
      )
    )
  )
-- group by removes duplicates rows introduced by the join using the pc_id_impact_search_consent as the same consent can be impacted by multiple consents
GROUP BY cel.pwa_id
, cel.search_consent_pc_id
, cel.consent_reference
, pcor.added_by_pwa_consent_id
, pcor.ended_by_pwa_consent_id
, pcor.role
, pcor.ou_id
, pcor.agreement
, pcor.migrated_organisation_name;