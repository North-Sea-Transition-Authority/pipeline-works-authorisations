ALTER TABLE ${datasource.user}.pad_project_information ADD (
    permanent_deposits_made NUMBER,
    future_app_submission_month Number(2),
    future_app_submission_year Number(4),
    temporary_deposits_made NUMBER,
    temporary_dep_description VARCHAR2(4000)
    );