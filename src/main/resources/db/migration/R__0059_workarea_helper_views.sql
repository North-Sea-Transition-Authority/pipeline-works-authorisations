-- helper views not mapped into entities have "wa_" prefix
CREATE OR REPLACE FORCE VIEW ${datasource.user}.wa_open_app_lookup AS
SELECT
  pad.pwa_application_id
, pad.id pad_id
, pa.pwa_id
, pad.status workarea_app_status
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


CREATE OR REPLACE FORCE VIEW ${datasource.user}.wa_open_public_notices AS
SELECT
  opn.pwa_application_id
, opn.public_notice_version
, pn.status public_notice_status
FROM ${datasource.user}.public_notices pn
  JOIN (
  -- get the version of the latest open public notice for an application to se get the current status
  SELECT
    woal.pwa_application_id, MAX (pn.version) public_notice_version
  FROM ${datasource.user}.wa_open_app_lookup woal
  JOIN ${datasource.user}.public_notices pn ON woal.pwa_application_id = pn.application_id
  WHERE pn.withdrawal_timestamp IS NULL
  GROUP BY woal.pwa_application_id
  ) opn ON pn.application_id = opn.pwa_application_id
WHERE opn.public_notice_version = pn.version
AND pn.status NOT IN ('WAITING', 'WITHDRAWN', 'ENDED', 'PUBLISHED');

CREATE OR REPLACE FORCE VIEW ${datasource.user}.wa_open_consultation_count AS
SELECT
  oa.pwa_application_id
, COALESCE(open_consultation_count.open_consultations, 0) open_consultations_count
FROM ${datasource.user}.wa_open_app_lookup oa
LEFT JOIN (
  SELECT
   MAX(creq.application_id) pwa_application_id
  , SUM(
      CASE
        WHEN COALESCE(creq.status, 'NONE') NOT IN ('WITHDRAWN', 'RESPONDED', 'NONE')
          THEN 1
        ELSE 0
      END
  ) open_consultations
  FROM ${datasource.user}.consultation_requests creq
  GROUP BY creq.application_id
) open_consultation_count ON open_consultation_count.pwa_application_id = oa.pwa_application_id;

CREATE OR REPLACE FORCE VIEW ${datasource.user}.wa_open_update_app_details AS
SELECT
  woal.pwa_application_id
, woal.pad_id
, aur.id aur_id
, aur.deadline_timestamp deadline_timestamp
, oaadh.deadline_date opt_approval_deadline_date
, pcoo.id pcoo_id
, CASE WHEN aur.id IS NOT NULL THEN 1 ELSE 0 END open_app_update
, CASE WHEN oaa.id IS NOT NULL AND pcoo.id IS NULL THEN 1 ELSE 0 END unresponded_option_approval
FROM ${datasource.user}.wa_open_app_lookup woal
LEFT JOIN ${datasource.user}.application_update_requests aur ON aur.pad_id = woal.pad_id AND aur.status = 'OPEN'
LEFT JOIN ${datasource.user}.options_application_approvals oaa ON oaa.pwa_application_id = woal.pwa_application_id
LEFT JOIN ${datasource.user}.options_app_appr_deadline_hist oaadh ON oaadh.options_app_approval_id = oaa.id AND oaadh.tip_flag = 1
LEFT JOIN ${datasource.user}.pad_confirmation_of_option pcoo ON pcoo.application_detail_id = woal.pad_id;

-- combine all flag info into single helper view with 1 row per open application
CREATE OR REPLACE FORCE VIEW ${datasource.user}.wa_application_flags AS
SELECT
  woal.pwa_application_id
, woal.pad_id
, woal.pwa_id
, woal.workarea_app_status -- status of the workarea version, not necessarily status of tip detail.
, woal.app_reference
, woal.application_type
, woal.latest_version_number
, woal.latest_draft_v_no
, woal.latest_submission_v_no
, woal.latest_satisfactory_v_no
, COALESCE(woal.latest_submission_v_no, woal.latest_draft_v_no) workarea_pad_version_no

-- app update info
, wouad.open_app_update
, wouad.unresponded_option_approval
, COALESCE(wouad.deadline_timestamp, wouad.opt_approval_deadline_date) update_deadline_timestamp

-- public notice info
, wopn.public_notice_version
, wopn.public_notice_status

-- consultations
, woco.open_consultations_count

FROM ${datasource.user}.wa_open_app_lookup woal
JOIN ${datasource.user}.wa_open_update_app_details wouad ON woal.pwa_application_id = wouad.pwa_application_id
JOIN ${datasource.user}.wa_open_consultation_count woco ON woal.pwa_application_id = woco.pwa_application_id
LEFT JOIN ${datasource.user}.wa_open_public_notices wopn ON woal.pwa_application_id = wopn.pwa_application_id;


CREATE OR REPLACE FORCE VIEW ${datasource.user}.workarea_app_user_tabs AS
SELECT
  waf.pwa_application_id
, CASE
    WHEN waf.workarea_app_status IN ('DRAFT', 'UPDATE_REQUESTED','AWAITING_APPLICATION_PAYMENT') OR waf.public_notice_status = 'APPLICANT_UPDATE'
    THEN 'FOR_ATTENTION'
    ELSE 'BACKGROUND'
  END app_user_workarea_category
, CASE
    WHEN waf.workarea_app_status = 'CASE_OFFICER_REVIEW'
       AND (
         -- Zero open consultations brings requires attention when no ongoing public notice or ongoing update
         (
           waf.open_consultations_count = 0
           AND waf.open_app_update = 0
           AND waf.unresponded_option_approval = 0
           AND COALESCE(waf.public_notice_status, 'NONE') = 'NONE'
         )
         -- attention required when latest submission requires marking as satisfactory
         OR (COALESCE(waf.latest_satisfactory_v_no, 0) < COALESCE(waf.latest_submission_v_no, 0))
         -- attention required if open public notice with the case officer
         OR (COALESCE(waf.public_notice_status, 'NONE') IN ('DRAFT', 'CASE_OFFICER_REVIEW'))
       )
    THEN 'FOR_ATTENTION'
  ELSE 'BACKGROUND'
  END case_officer_workarea_category
, CASE
    WHEN waf.workarea_app_status IN ('INITIAL_SUBMISSION_REVIEW', 'CONSENT_REVIEW') OR (COALESCE(waf.public_notice_status, 'NONE') = 'MANAGER_APPROVAL')
      THEN 'FOR_ATTENTION'
    WHEN waf.latest_draft_v_no = 1 THEN 'NONE' -- first drafts should not be in a PWA_managers tabs at all. Required as managers are not directly associated with cases for filtering.
  ELSE 'BACKGROUND'
  END pwa_manager_workarea_category
FROM ${datasource.user}.wa_application_flags waf;