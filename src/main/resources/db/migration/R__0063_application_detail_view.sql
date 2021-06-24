CREATE OR REPLACE FORCE VIEW ${datasource.user}.application_detail_view AS
SELECT
  p.id pwa_id
, pd.id pwa_detail_id
, pad.pwa_application_id
, pad.id pwa_application_detail_id

, pd.reference pwa_reference
, pa.app_reference pad_reference
, pa.application_type

, pad.status pad_status
, pad.created_timestamp pad_created_timestamp
, pad.submitted_timestamp pad_submitted_timestamp
, (
    SELECT MAX(pir.init_review_approved_timestamp)
    FROM ${datasource.user}.pad_initial_review pir
    WHERE pir.application_detail_id = pad.id
) pad_init_review_approved_ts
, pad.confirmed_satisfactory_ts pad_confirmed_satisfactory_ts
, pad.status_last_modified_timestamp pad_status_timestamp
, pad.tip_flag
, pad.version_no
, COALESCE(pad.submitted_as_fast_track_flag, 0) submitted_as_fast_track_flag
-- TODO: remove this as not required as app search param or as display unit on app workarea/search view
, CASE WHEN pad.tip_flag = 1 AND pad.confirmed_satisfactory_ts IS NOT NULL THEN 1 ELSE 0 END tip_version_satisfactory_flag

, paa.assignee_person_id case_officer_person_id
, paa.assignee_name case_officer_name

, ppi.project_name pad_project_name
, ppi.proposed_start_timestamp pad_proposed_start_timestamp
, (
    SELECT LISTAGG(COALESCE(df.field_name, pf.field_name_manual_entry), ';;;;') WITHIN GROUP(ORDER BY 1) -- this will do for now. might need to change as requirements become more specific.
    FROM ${datasource.user}.pad_fields pf
    LEFT JOIN ${datasource.user}.devuk_fields df ON pf.field_id = df.field_id
    WHERE pf.application_detail_id = pad.id
  ) pad_field_name_list

, (
  -- when dealing with a migrated case, include all HOLDERS, even if not successfully mapped to avoid hiding expected info
  SELECT LISTAGG(COALESCE(vphou.ou_name, vphou.migrated_organisation_name), ';;;;') WITHIN GROUP(ORDER BY 1)
  FROM ${datasource.user}.vw_pwa_holder_org_units vphou
  WHERE vphou.pwa_id = p.id
) pwa_holder_name_list

, (
  -- holder can't be treaties so dont need to worry.
  SELECT LISTAGG(pou.name, ';;;;') WITHIN GROUP(ORDER BY 1)
  FROM ${datasource.user}.pad_organisation_roles por
  JOIN ${datasource.user}.portal_organisation_units pou ON por.ou_id = pou.ou_id
  WHERE por.application_detail_id = pad.id
  AND por.role = 'HOLDER'
) pad_holder_name_list
-- TODO: remove this as not required as app search param or as display unit on app workarea/search view
, CASE WHEN (
      SELECT COUNT(*)
      FROM ${datasource.user}.consultation_requests creq
      WHERE creq.status NOT IN ('WITHDRAWN', 'RESPONDED')
      AND creq.application_id = pad.pwa_application_id
    ) > 0 THEN 1
    ELSE 0
  END open_consultation_req_flag
, wopn.public_notice_status
, CASE WHEN wouad.pad_id IS NOT NULL THEN 1 ELSE 0 END open_update_request_flag
, COALESCE(wouad.deadline_timestamp, wouad.opt_approval_deadline_date) open_update_deadline_ts
-- TODO: remove this as not required as search param or as display unit on app workarea/search view
, CASE WHEN pcr.id IS NOT NULL THEN 1 ELSE 0 END open_consent_review_flag
FROM ${datasource.user}.pwa_application_details pad -- want 1 row per detail for maximum query flexibility. intended to be the only introduced cardinality
JOIN ${datasource.user}.pwa_applications pa ON pad.pwa_application_id = pa.id
JOIN ${datasource.user}.pad_status_versions psv ON pa.id = psv.pwa_application_id
JOIN ${datasource.user}.pwas p ON pa.pwa_id = p.id
JOIN ${datasource.user}.pwa_details pd ON pd.pwa_id = p.id AND pd.end_timestamp IS NULL
LEFT JOIN ${datasource.user}.pwa_app_assignments paa ON paa.pwa_application_id = pad.pwa_application_id AND paa.assignment = 'CASE_OFFICER'
LEFT JOIN ${datasource.user}.pad_project_information ppi ON ppi.application_detail_id = pad.id
LEFT JOIN ${datasource.user}.wa_open_public_notices wopn ON wopn.pwa_application_id = pa.id
LEFT JOIN ${datasource.user}.wa_open_update_app_details wouad ON wouad.pad_id = pad.id AND (wouad.open_app_update = 1 OR wouad.unresponded_option_approval = 1)
LEFT JOIN ${datasource.user}.pad_consent_reviews pcr ON pcr.pad_id = pad.id AND pcr.end_timestamp IS NULL;
/