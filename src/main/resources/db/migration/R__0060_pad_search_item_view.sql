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
, pad.approved_timestamp pad_approved_timestamp
, pad.status_last_modified_timestamp pad_status_timestamp
, pad.tip_flag

, ppi.project_name pad_project_name
, ppi.proposed_start_timestamp pad_proposed_start_timestamp
, (
    SELECT LISTAGG(COALESCE(df.field_name, pf.field_name_manual_entry), ';;;;') WITHIN GROUP(ORDER BY 1) -- this will do for now. might need to change as requirements become more specific.
    FROM pwa_mh.pad_fields pf
    LEFT JOIN pwa_mh.devuk_fields df ON pf.field_id = df.field_id
    WHERE pf.application_detail_id = pad.id
  ) pad_field_name_list
FROM pwa_mh.pwa_application_details pad -- want 1 row per detail for maximum query flexibility. intended to be the only introduced cardinality
JOIN ${datasource.user}.pwa_applications pa ON pad.pwa_application_id = pa.id
JOIN ${datasource.user}.pwas p ON pa.pwa_id = p.id
JOIN ${datasource.user}.pwa_details pd ON pd.pwa_id = p.id

LEFT JOIN pwa_mh.pad_project_information ppi ON ppi.application_detail_id = pad.id
WHERE pd.end_timestamp IS NULL; 

