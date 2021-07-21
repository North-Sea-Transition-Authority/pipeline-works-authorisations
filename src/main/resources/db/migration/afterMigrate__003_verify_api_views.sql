/* verify api_vw_pipeline_as_built_data only has 1 row per pipeline */
DECLARE
  l_problem_pipeline_count NUMBER;
BEGIN

  SELECT COUNT(*)
  INTO l_problem_pipeline_count
  FROM (
    SELECT avpabd.pipeline_id, COUNT(*)
    FROM ${datasource.user}.api_vw_pipeline_as_built_data avpabd
    GROUP BY avpabd.pipeline_id
    HAVING COUNT(*) > 1
  );

  IF(l_problem_pipeline_count != 0) THEN
    RAISE_APPLICATION_ERROR(-20801, 'FAILED_TO_VERIFY "api_vw_pipeline_as_built_data". l_problem_pipeline_count: ' || l_problem_pipeline_count);
  END IF;

END;
/

/*  verify api_vw_current_pipeline_data has only 1 row per pipeline */
DECLARE
  l_problem_pipeline_count NUMBER;
BEGIN

  SELECT COUNT(*)
  INTO l_problem_pipeline_count
  FROM (
    SELECT pipeline_id, COUNT(*)
    FROM ${datasource.user}.api_vw_current_pipeline_data
    GROUP BY pipeline_id
    HAVING COUNT(*) > 1
  );

  IF(l_problem_pipeline_count != 0) THEN
    RAISE_APPLICATION_ERROR(-20801, 'FAILED_TO_VERIFY "api_vw_current_pipeline_data". l_problem_pipeline_count: ' || l_problem_pipeline_count);
  END IF;

END;