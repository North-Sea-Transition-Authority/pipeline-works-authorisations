CREATE OR REPLACE FORCE VIEW ${datasource.user}.workarea_search_items AS
SELECT
  adv.pwa_id
, adv.pwa_detail_id
, adv.pwa_application_id
, adv.pwa_application_detail_id
, adv.pwa_reference
, adv.pad_reference
, adv.application_type
, adv.resource_type
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
, CASE
    WHEN adv.pad_submitted_timestamp IS NULL THEN 0
    ELSE 1
  END submitted_flag
FROM ${datasource.user}.wa_application_flags waf
JOIN ${datasource.user}.application_detail_view adv ON waf.pwa_application_id = adv.pwa_application_id AND waf.workarea_pad_version_no = adv.version_no;
/

DECLARE
  l_command VARCHAR2(4000) := 'DROP VIEW ${datasource.user}.pad_search_items';
BEGIN
  EXECUTE IMMEDIATE l_command;
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN
      RAISE;
    END IF;
END;
/
