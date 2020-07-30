ALTER TABLE ${datasource.user}.pad_pipelines
ADD temporary_number NUMBER;

UPDATE ${datasource.user}.pad_pipelines pp
SET pp.temporary_number = (
  SELECT row_num
  FROM (
    SELECT
        inner_pp.id
      , ROW_NUMBER() OVER(
        PARTITION BY inner_pp.pad_id
        ORDER BY inner_pp.id ASC
      ) row_num
    FROM ${datasource.user}.pad_pipelines inner_pp
  ) pp_agg
  WHERE pp_agg.id = pp.id
);