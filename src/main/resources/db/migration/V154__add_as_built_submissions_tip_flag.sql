ALTER TABLE ${datasource.user}.as_built_notif_submissions
ADD tip_flag NUMBER;


UPDATE ${datasource.user}.as_built_notif_submissions
SET tip_flag =
        CASE WHEN id IN (
            SELECT id
            FROM (
                    SELECT ${datasource.user}.as_built_notif_submissions.*, ROW_NUMBER() OVER (PARTITION BY ${datasource.user}.as_built_notif_submissions.as_built_notif_pipeline_id ORDER BY ${datasource.user}.as_built_notif_submissions.submitted_timestamp DESC) AS row_no
                    FROM  ${datasource.user}.as_built_notif_submissions
                )
            WHERE row_no = 1) THEN 1
            ELSE 0
        END;

ALTER TABLE ${datasource.user}.as_built_notif_submissions
MODIFY tip_flag NUMBER NOT NULL;