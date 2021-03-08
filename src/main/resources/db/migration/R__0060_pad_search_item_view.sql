CREATE OR REPLACE VIEW ${datasource.user}.workarea_search_items (
  pwa_id
, pwa_detail_id
, pwa_application_id
, pwa_application_detail_id
, pwa_reference
, pad_reference
, application_type
, pad_status
, pad_created_timestamp
, pad_submitted_timestamp
, pad_init_review_approved_ts
, pad_status_timestamp
, tip_flag
, version_no
, submitted_as_fast_track_flag
, tip_version_satisfactory_flag
, case_officer_person_id
, case_officer_name
, pad_project_name
, pad_proposed_start_timestamp
, pad_field_name_list
, pwa_holder_name_list
, pad_holder_name_list
, open_consultation_req_flag
, public_notice_status
, open_update_request_flag
, open_consent_review_flag
) AS
WITH open_update_app_details AS (
  SELECT
    pad.pwa_application_id
  , pad.id pad_id
  , aur.id aur_id
  , pcoo.id pcoo_id
  , CASE WHEN aur.id IS NOT NULL THEN 1 ELSE 0 END open_app_update
  , CASE WHEN oaa.id IS NOT NULL AND pcoo.id IS NULL THEN 1 ELSE 0 END unresponded_option_approval
  FROM ${datasource.user}.pwa_application_details pad
  LEFT JOIN ${datasource.user}.application_update_requests aur ON aur.pad_id = pad.id AND aur.status = 'OPEN'
  LEFT JOIN ${datasource.user}.options_application_approvals oaa ON oaa.pwa_application_id = pad.pwa_application_id
  LEFT JOIN ${datasource.user}.pad_confirmation_of_option pcoo ON pcoo.application_detail_id = pad.id
)
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
, pad.init_review_approved_timestamp pad_init_review_approved_ts
, pad.status_last_modified_timestamp pad_status_timestamp
, pad.tip_flag
, pad.version_no
, COALESCE(pad.submitted_as_fast_track_flag, 0) submitted_as_fast_track_flag
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
  SELECT LISTAGG(COALESCE(pou.name, pcor.migrated_organisation_name), ';;;;') WITHIN GROUP(ORDER BY 1)
  FROM ${datasource.user}.pwa_consents pc
  JOIN ${datasource.user}.pwa_consent_organisation_roles pcor ON pc.id = pcor.added_by_pwa_consent_id
  LEFT JOIN ${datasource.user}.portal_organisation_units pou ON pcor.ou_id = pou.ou_id
  WHERE pc.pwa_id = p.id
  AND pcor.ended_by_pwa_consent_id IS NULL
  AND pcor.role = 'HOLDER'
  ) pwa_holder_name_list

, (
  -- holder can't be treaties so dont need to worry.
  SELECT LISTAGG(pou.name, ';;;;') WITHIN GROUP(ORDER BY 1)
  FROM ${datasource.user}.pad_organisation_roles por
  JOIN ${datasource.user}.portal_organisation_units pou ON por.ou_id = pou.ou_id
  WHERE por.application_detail_id = pad.id
  AND por.role = 'HOLDER'
) pad_holder_name_list

, CASE WHEN (
      SELECT COUNT(*)
      FROM ${datasource.user}.consultation_requests creq
      WHERE creq.status NOT IN ('WITHDRAWN', 'RESPONDED')
      AND creq.application_id = pad.pwa_application_id
    ) > 0 THEN 1
    ELSE 0
  END open_consultation_req_flag
, pn.status public_notice_status
, CASE WHEN ouad.pad_id IS NOT NULL THEN 1 ELSE 0 END open_update_request_flag
, CASE WHEN pcr.id IS NOT NULL THEN 1 ELSE 0 END open_consent_review_flag
FROM ${datasource.user}.pwa_application_details pad -- want 1 row per detail for maximum query flexibility. intended to be the only introduced cardinality
JOIN ${datasource.user}.pwa_applications pa ON pad.pwa_application_id = pa.id
JOIN ${datasource.user}.pad_status_versions psv ON pa.id = psv.pwa_application_id
JOIN ${datasource.user}.pwas p ON pa.pwa_id = p.id
JOIN ${datasource.user}.pwa_details pd ON pd.pwa_id = p.id
LEFT JOIN ${datasource.user}.pwa_app_assignments paa ON paa.pwa_application_id = pad.pwa_application_id AND paa.assignment = 'CASE_OFFICER'
LEFT JOIN ${datasource.user}.pad_project_information ppi ON ppi.application_detail_id = pad.id
LEFT JOIN ${datasource.user}.public_notices pn ON pn.application_id = pa.id AND pn.status NOT IN ('WITHDRAWN', 'ENDED')
LEFT JOIN open_update_app_details ouad ON ouad.pad_id = pad.id AND (ouad.open_app_update = 1 OR ouad.unresponded_option_approval = 1)
LEFT JOIN ${datasource.user}.pad_consent_reviews pcr ON pcr.pad_id = pad.id AND pcr.end_timestamp IS NULL
WHERE pd.end_timestamp IS NULL
AND (

  -- if there's a submitted version, always show the latest submitted version
  (psv.latest_submission_ts IS NOT NULL AND pad.submitted_timestamp = psv.latest_submission_ts)

  OR

  -- otherwise there should be a draft version we can show instead
  (psv.latest_submission_ts IS NULL AND pad.version_no = psv.latest_draft_v_no)

);


