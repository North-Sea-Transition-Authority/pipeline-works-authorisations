ALTER TABLE ${datasource.user}.pad_project_information ADD csp_option_selected NUMBER(1);
ALTER TABLE ${datasource.user}.pad_project_information ADD csp_confirmation_flag NUMBER(1);
ALTER TABLE ${datasource.user}.pad_project_information ADD csp_not_selected_reason VARCHAR(4000);
