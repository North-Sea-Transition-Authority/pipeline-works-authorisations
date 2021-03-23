CREATE OR REPLACE VIEW ${datasource.user}.pad_status_versions
AS
SELECT
  pad.pwa_application_id
, MAX(pad.version_no) latest_version_number
, MAX( CASE WHEN pad.submitted_timestamp IS NOT NULL THEN pad.version_no END) latest_submission_v_no
, MAX( CASE WHEN pad.submitted_timestamp IS NOT NULL THEN pad.submitted_timestamp END) latest_submission_ts
, MAX( CASE WHEN pad.confirmed_satisfactory_ts IS NOT NULL THEN pad.version_no END) latest_satisfactory_v_no
, MAX( CASE WHEN pad.confirmed_satisfactory_ts IS NOT NULL THEN pad.confirmed_satisfactory_ts END) latest_satisfactory_ts
, MAX(CASE
  WHEN pad.status IN ('DRAFT', 'UPDATE_REQUESTED')
    THEN pad.version_no
  END) latest_draft_v_no
FROM ${datasource.user}.pwa_application_details pad
GROUP BY pad.pwa_application_id;

