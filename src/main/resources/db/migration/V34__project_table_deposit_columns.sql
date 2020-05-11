ALTER TABLE ${datasource.user}.pad_project_information ADD (
    permanent_deposits_made INTEGER CHECK(permanent_deposits_made IN (0, 1) OR permanent_deposits_made IS NULL),
    future_app_submission_month Number(2),
    future_app_submission_year Number(4),
    temporary_deposits_made INTEGER CHECK(temporary_deposits_made IN (0, 1) OR temporary_deposits_made IS NULL),
    temporary_dep_description VARCHAR2(4000)
    );