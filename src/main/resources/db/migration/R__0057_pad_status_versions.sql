CREATE OR REPLACE VIEW ${datasource.user}.pad_status_versions
AS
SELECT
  pwa_application_id
, MAX(latest_submission_ts) latest_submission_ts
, MAX(latest_satisfactory_ts) latest_satisfactory_ts
, MAX(latest_draft_v_no) latest_draft_v_no
FROM (
  SELECT
    pad.pwa_application_id
  , MAX(pad.submitted_timestamp) latest_submission_ts
  , MAX(pad.confirmed_satisfactory_ts) latest_satisfactory_ts
  , NULL latest_draft_v_no
  FROM ${datasource.user}.pwa_application_details pad
  where status != 'DRAFT'
  GROUP BY pad.pwa_application_id
  UNION ALL
  SELECT
    pad.pwa_application_id
  , NULL
  , NULL
  , MAX(pad.version_no) latest_draft_v_no
  FROM ${datasource.user}.pwa_application_details pad
  WHERE pad.status = 'DRAFT'
  GROUP BY pad.pwa_application_id
)
GROUP BY pwa_application_id