-- helper view
CREATE OR REPLACE FORCE VIEW ${datasource.user}.open_applications AS
SELECT
  pad.pwa_application_id
, pad.id pad_id
, pa.pwa_id
, pad.status
, pa.app_reference
, pa.application_type
, psv.latest_version_number
, psv.latest_draft_v_no
, psv.latest_submission_v_no
, psv.latest_satisfactory_v_no
FROM ${datasource.user}.pwa_application_details pad
JOIN ${datasource.user}.pad_status_versions psv ON pad.pwa_application_id = psv.pwa_application_id
JOIN ${datasource.user}.pwa_applications pa ON pad.pwa_application_id = pa.id
-- only get the last submitted version, or first draft if none submitted
WHERE NOT (pad.status IN ('COMPLETE', 'WITHDRAWN', 'DELETED'))
AND pad.version_no = COALESCE(psv.latest_submission_v_no, psv.latest_draft_v_no);


CREATE OR REPLACE FORCE VIEW ${datasource.user}.open_public_notices AS
SELECT
  opn.pwa_application_id
, opn.public_notice_version
, pn.status public_notice_status
FROM ${datasource.user}.public_notices pn
  JOIN (
  -- get the version of the latest open public notice for an application to se get the current status
  SELECT
  oa.pwa_application_id, MAX (pn.version) public_notice_version
  FROM ${datasource.user}.open_applications oa
  JOIN ${datasource.user}.public_notices pn ON oa.pwa_application_id = pn.application_id
  WHERE pn.withdrawal_timestamp IS NULL
  GROUP BY oa.pwa_application_id
  ) opn ON pn.application_id = opn.pwa_application_id
WHERE opn.public_notice_version = pn.version
AND pn.status NOT IN ('WAITING', 'WITHDRAWN', 'PUBLISHED', 'ENDED');


CREATE OR REPLACE FORCE VIEW ${datasource.user}.workarea_search_items (
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
, open_update_deadline_ts
, open_consent_review_flag
) AS
WITH open_update_app_details AS (
  SELECT
    pad.pwa_application_id
  , pad.id pad_id
  , aur.id aur_id
  , aur.deadline_timestamp deadline_timestamp
  , oaadh.deadline_date opt_approval_deadline_date
  , pcoo.id pcoo_id
  , CASE WHEN aur.id IS NOT NULL THEN 1 ELSE 0 END open_app_update
  , CASE WHEN oaa.id IS NOT NULL AND pcoo.id IS NULL THEN 1 ELSE 0 END unresponded_option_approval
  FROM ${datasource.user}.pwa_application_details pad
  LEFT JOIN ${datasource.user}.application_update_requests aur ON aur.pad_id = pad.id AND aur.status = 'OPEN'
  LEFT JOIN ${datasource.user}.options_application_approvals oaa ON oaa.pwa_application_id = pad.pwa_application_id
  LEFT JOIN ${datasource.user}.options_app_appr_deadline_hist oaadh ON oaadh.options_app_approval_id = oaa.id AND oaadh.tip_flag = 1
  LEFT JOIN ${datasource.user}.pad_confirmation_of_option pcoo ON pcoo.application_detail_id = pad.id
)
SELECT
  p.id pwa_id
, pd.id pwa_detail_id
, pad.pwa_application_id
, pad.id pwa_application_detail_id

, pd.reference pwa_reference
, oa.app_reference pad_reference
, oa.application_type

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

, CASE WHEN (
      SELECT COUNT(*)
      FROM ${datasource.user}.consultation_requests creq
      WHERE creq.status NOT IN ('WITHDRAWN', 'RESPONDED')
      AND creq.application_id = pad.pwa_application_id
    ) > 0 THEN 1
    ELSE 0
  END open_consultation_req_flag
, opn.public_notice_status
, CASE WHEN ouad.pad_id IS NOT NULL THEN 1 ELSE 0 END open_update_request_flag
, COALESCE(ouad.deadline_timestamp, ouad.opt_approval_deadline_date) deadline_timestamp
, CASE WHEN pcr.id IS NOT NULL THEN 1 ELSE 0 END open_consent_review_flag
FROM ${datasource.user}.open_applications oa
JOIN ${datasource.user}.pwa_application_details pad ON oa.pad_id = pad.id
JOIN ${datasource.user}.pwas p ON p.id = oa.pwa_id
JOIN ${datasource.user}.pwa_details pd ON pd.pwa_id = p.id
LEFT JOIN ${datasource.user}.pwa_app_assignments paa ON paa.pwa_application_id = pad.pwa_application_id AND paa.assignment = 'CASE_OFFICER'
LEFT JOIN ${datasource.user}.pad_project_information ppi ON ppi.application_detail_id = pad.id
LEFT JOIN ${datasource.user}.open_public_notices opn ON opn.pwa_application_id = pad.pwa_application_id
LEFT JOIN open_update_app_details ouad ON ouad.pad_id = pad.id AND (ouad.open_app_update = 1 OR ouad.unresponded_option_approval = 1)
LEFT JOIN ${datasource.user}.pad_consent_reviews pcr ON pcr.pad_id = pad.id AND pcr.end_timestamp IS NULL
WHERE pd.end_timestamp IS NULL;



CREATE OR REPLACE FORCE VIEW ${datasource.user}.workarea_app_lifecycle_events (
  pwa_application_id
, flag
) AS
-- case officer specific event for when app is not being worked in on a different context by some other user
SELECT oa.pwa_application_id, 'APPLICATION_NOT_BEING_WORKED_ON' flag
FROM ${datasource.user}.open_applications oa
LEFT JOIN (
  SELECT MAX(creq.application_id) pwa_application_id
  FROM ${datasource.user}.consultation_requests creq --ON creq.application_id = oa.pwa_application_id
  GROUP BY creq.application_id
  HAVING SUM(
    CASE
      WHEN COALESCE(creq.status, 'NONE') NOT IN ('WITHDRAWN', 'RESPONDED', 'NONE')
        THEN 1
      ELSE 0
      END) = 0
) apps_with_no_open_consultation ON apps_with_no_open_consultation.pwa_application_id = oa.pwa_application_id
LEFT JOIN open_public_notices opn ON opn.pwa_application_id = oa.pwa_application_id
WHERE oa.status = 'CASE_OFFICER_REVIEW' -- event is only generated when app in this status
-- if theres an open consultation, case officer not interested
AND apps_with_no_open_consultation.pwa_application_id IS NOT NULL
-- if theres an open public notice with the the applicant or pwa manager
AND COALESCE(opn.public_notice_status, 'NOT_OPEN') NOT IN ('APPLICANT_UPDATE', 'MANAGER_APPROVAL')
-- if theres a ongoing update with the applicant
AND  COALESCE(oa.latest_draft_v_no, 0) < oa.latest_submission_v_no
-- TODO PWA-1172: do we even need the latest satisfactory version check? we want to see the case unless the above cases are true, that doesnt care about the satisfactory version check.
UNION ALL
-- public notice attention required events
SELECT
  opn.pwa_application_id
, CASE
    WHEN opn.public_notice_status = 'APPLICANT_UPDATE' THEN 'PUBLIC_NOTICE_WAITING_ON_APP_CONTACT'
    WHEN opn.public_notice_status = 'MANAGER_APPROVAL' THEN 'PUBLIC_NOTICE_WAITING_ON_PWA_MANAGER'
    ELSE 'PUBLIC_NOTICE_WAITING_ON_CASE_OFFICER'
  END flag
FROM open_public_notices opn
UNION ALL
-- mutually exclusive status based events
SELECT
  oa.pwa_application_id
, CASE
    WHEN oa.status IN ('DRAFT', 'UPDATE_REQUESTED') THEN 'UPDATE_REQUIRED'
    WHEN oa.status = 'INITIAL_SUBMISSION_REVIEW' THEN 'INITIAL_REVIEW_REQUIRED'
    WHEN oa.status = 'AWAITING_APPLICATION_PAYMENT' THEN 'PAYMENT_REQUIRED'
    WHEN oa.status = 'CONSENT_REVIEW' THEN 'CONSENT_DOCUMENT_REQUIRES_PWA_MANAGER_SIGN_OFF'
  END flag
FROM open_applications oa
WHERE oa.status IN ('DRAFT', 'UPDATE_REQUESTED', 'INITIAL_SUBMISSION_REVIEW', 'AWAITING_APPLICATION_PAYMENT', 'CONSENT_REVIEW')
-- UNION ALL
-- -- satisfactory flag not set on last submitted version
-- SELECT oa.pwa_application_id
-- , 'SATISFACTORY_VERSION_CHECK_REQUIRED' flag
-- FROM open_applications oa
-- JOIN ${datasource.user}.pad_status_versions psv ON psv.pwa_application_id = oa.pwa_application_id
-- WHERE psv.latest_satisfactory_v_no != psv.latest_submission_v_no
-- AND psv.latest_submission_v_no IS NOT NULL
;
