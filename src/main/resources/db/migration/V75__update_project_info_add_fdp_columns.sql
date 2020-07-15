ALTER TABLE ${datasource.user}.pad_project_information ADD (
    fdp_option_selected INTEGER CHECK(fdp_option_selected IN (0, 1)),
    fdp_confirmation_flag INTEGER CHECK(fdp_confirmation_flag IN (0, 1)),
    fdp_not_selected_reason VARCHAR2(4000)
);
