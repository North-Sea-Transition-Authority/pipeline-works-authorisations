CREATE OR REPLACE VIEW ${datasource.user}.as_built_notif_workarea_view AS
SELECT
  ng.id ng_id
, ng.reference ng_reference
, ngd.deadline_date
, CASE
    WHEN pa.application_type = 'OPTIONS_VARIATION' THEN pc.consent_timestamp
    ELSE ppi.latest_completion_timestamp
  END AS completion_timestamp
, ngsh.status
, ppi.project_name
, pwad.pwa_id
, pwad.reference pwa_reference
, pc.id consent_id
, (SELECT LISTAGG(COALESCE(vphou.ou_name, vphou.migrated_organisation_name), ';;;;') WITHIN GROUP(ORDER BY 1)
   FROM ${datasource.user}.vw_pwa_holder_org_units vphou
   WHERE vphou.pwa_id = pwad.pwa_id) pwa_holder_name_list
FROM ${datasource.user}.as_built_notification_groups ng
JOIN ${datasource.user}.as_built_notif_grp_details ngd ON ngd.as_built_notification_group_id = ng.id
JOIN ${datasource.user}.as_built_notif_grp_status_hist ngsh ON ngsh.as_built_notification_group_id = ng.id
-- join consents
JOIN ${datasource.user}.pwa_consents pc ON ng.pwa_consent_id = pc.id
-- join app details
JOIN ${datasource.user}.pwa_application_details pad ON pc.source_pwa_application_id = pad.pwa_application_id
JOIN ${datasource.user}.pwa_applications pa ON pad.pwa_application_id = pa.id
-- join master pwa detail
JOIN ${datasource.user}.pwa_details pwad ON pc.pwa_id = pwad.pwa_id
JOIN ${datasource.user}.pad_project_information ppi ON ppi.application_detail_id = pad.id
-- filter out ended group details
WHERE ngd.ended_timestamp IS NULL
-- filter out ended statuses
AND ngsh.ended_timestamp IS NULL
-- only get the latest app detail for an application
AND pad.tip_flag = 1
-- only get the latest pwa detail for the reference
AND pwad.end_timestamp IS NULL;