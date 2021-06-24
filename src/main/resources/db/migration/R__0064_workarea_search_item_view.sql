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
SELECT
  adv.pwa_id
, adv.pwa_detail_id
, adv.pwa_application_id
, adv.pwa_application_detail_id
, adv.pwa_reference
, adv.pad_reference
, adv.application_type
, adv.pad_status
, adv.pad_created_timestamp
, adv.pad_submitted_timestamp
, adv.pad_init_review_approved_ts
, adv.pad_status_timestamp
, adv.tip_flag
, adv.version_no
, adv.submitted_as_fast_track_flag
, adv.tip_version_satisfactory_flag
, adv.case_officer_person_id
, adv.case_officer_name
, adv.pad_project_name
, adv.pad_proposed_start_timestamp
, adv.pad_field_name_list
, adv.pwa_holder_name_list
, adv.pad_holder_name_list
, adv.open_consultation_req_flag
, adv.public_notice_status
, adv.open_update_request_flag
, adv.open_update_deadline_ts
, adv.open_consent_review_flag
FROM ${datasource.user}.wa_application_flags waf
JOIN ${datasource.user}.application_detail_view adv ON waf.pwa_application_id = adv.pwa_application_id AND waf.workarea_pad_version_no = adv.version_no

