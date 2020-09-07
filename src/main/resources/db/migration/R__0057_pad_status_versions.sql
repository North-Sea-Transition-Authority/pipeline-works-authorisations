CREATE OR REPLACE VIEW ${datasource.user}.pad_status_versions
AS
  SELECT pwa_application_id
  , CASE
      WHEN GREATEST(
        COALESCE (max_initial_sub_review_vers, 0),
        COALESCE (max_case_officer_review_vers, 0)
      ) != 0 THEN GREATEST(
        COALESCE (max_initial_sub_review_vers, 0),
        COALESCE (max_case_officer_review_vers, 0)
      ) END last_submitted_version
  , max_draft_version
  , max_initial_sub_review_vers
  , max_case_officer_review_vers
  FROM (
  SELECT *
  FROM (
    SELECT pad.pwa_application_id
         , pad.status
         , MAX (pad.version_no) version_number
    FROM ${datasource.user}.pwa_application_details pad
    WHERE pad.version_no IS NOT NULL
    GROUP BY pad.pwa_application_id, pad.status
  )  PIVOT (
    MAX (version_number) FOR status IN (
      'DRAFT' AS max_draft_version,
      'INITIAL_SUBMISSION_REVIEW' AS max_initial_sub_review_vers,
      'CASE_OFFICER_REVIEW' AS max_case_officer_review_vers
      )
    )
  );