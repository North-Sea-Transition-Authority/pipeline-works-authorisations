ALTER TABLE ${datasource.user}.pad_project_information ADD (
    is_permanent_deposits_made Number(*),
    future_app_submission_month Number(2),
    future_app_submission_year Number(4),
    is_temporary_deposits_made Number(*),
    temporary_dep_description VARCHAR2(4000)
    );