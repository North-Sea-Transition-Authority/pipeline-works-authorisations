UPDATE ${datasource.user}.pad_pipeline_tech_info pti SET pti.estimated_asset_life = pti.estimated_field_life WHERE pti.estimated_asset_life IS NULL;
ALTER TABLE ${datasource.user}.pad_pipeline_tech_info DROP COLUMN estimated_field_life;
