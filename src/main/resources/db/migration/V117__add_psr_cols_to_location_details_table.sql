ALTER TABLE ${datasource.user}.pad_location_details ADD (
    psr_notification_submitted INTEGER CHECK(psr_notification_submitted IN (0, 1)),
    psr_submitted_month INTEGER,
    psr_submitted_year INTEGER,
    psr_expected_submission_month INTEGER,
    psr_expected_submission_year INTEGER
);