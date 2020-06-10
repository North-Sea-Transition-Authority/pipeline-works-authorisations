CREATE OR REPLACE VIEW ${datasource.user}.pad_search_items
AS
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
, COALESCE(pad.submitted_as_fast_track_flag, 0) submitted_as_fast_track_flag

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


FROM ${datasource.user}.pwa_application_details pad -- want 1 row per detail for maximum query flexibility. intended to be the only introduced cardinality
JOIN ${datasource.user}.pwa_applications pa ON pad.pwa_application_id = pa.id
JOIN ${datasource.user}.pwas p ON pa.pwa_id = p.id
JOIN ${datasource.user}.pwa_details pd ON pd.pwa_id = p.id

LEFT JOIN ${datasource.user}.pwa_app_assignments paa ON paa.pwa_application_id = pad.pwa_application_id AND paa.assignment = 'CASE_OFFICER'

LEFT JOIN ${datasource.user}.pad_project_information ppi ON ppi.application_detail_id = pad.id
WHERE pd.end_timestamp IS NULL;

