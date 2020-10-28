ALTER TABLE ${datasource.user}.pad_permanent_deposits
    ADD deposit_for_consented_pipeline INTEGER CHECK(deposit_for_consented_pipeline IN (0, 1) OR deposit_for_consented_pipeline IS NULL);

ALTER TABLE ${datasource.user}.pad_permanent_deposits
    ADD dep_for_other_app_pipelines INTEGER CHECK(dep_for_other_app_pipelines IN (0, 1) OR dep_for_other_app_pipelines IS NULL);

ALTER TABLE ${datasource.user}.pad_permanent_deposits
    ADD app_ref_and_pipeline_num VARCHAR2(4000);
