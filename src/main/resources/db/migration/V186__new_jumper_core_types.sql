UPDATE ${datasource.user}.pad_pipelines
SET pipeline_type = 'HYDRAULIC_JUMPER_MULTI_CORE'
WHERE pipeline_type = 'HYDRAULIC_JUMPER';

UPDATE ${datasource.user}.pad_pipelines
SET pipeline_type = 'CONTROL_JUMPER_SINGLE_CORE'
WHERE pipeline_type = 'CONTROL_JUMPER';

UPDATE ${datasource.user}.pipeline_details
SET pipeline_type = 'HYDRAULIC_JUMPER_MULTI_CORE'
WHERE pipeline_type = 'HYDRAULIC_JUMPER';

UPDATE ${datasource.user}.pipeline_details
SET pipeline_type = 'CONTROL_JUMPER_SINGLE_CORE'
WHERE pipeline_type = 'CONTROL_JUMPER';