ALTER TABLE ${datasource.user}.pad_pipelines
ADD temporary_number NUMBER;

UPDATE ${datasource.user}.pad_pipelines pp
SET temporary_number = (
  SELECT q.row_num
  FROM (
    WITH non_consented_pipelines AS (
      SELECT inner_pp.*
      FROM ${datasource.user}.pad_pipelines inner_pp
      MINUS
      SELECT inner_pp2.*
      FROM ${datasource.user}.pad_pipelines inner_pp2
      JOIN ${datasource.user}.pipeline_details pd ON inner_pp2.pipeline_id = pd.pipeline_id
    )
    SELECT
      ROW_NUMBER() OVER(
        PARTITION BY ncp.pad_id
        ORDER BY ncp.id ASC
      ) row_num
    , ncp.*
    FROM non_consented_pipelines ncp
  ) q
  WHERE q.id = pp.id
);

UPDATE ${datasource.user}.pad_pipelines pp
SET pp.pipeline_ref = 'TEMPORARY ' || pp.temporary_number
WHERE pp.temporary_number IS NOT NULL; 
