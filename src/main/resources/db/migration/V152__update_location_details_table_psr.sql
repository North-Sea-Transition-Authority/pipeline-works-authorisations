ALTER TABLE ${datasource.user}.pad_location_details ADD (
    psr_submitted_option VARCHAR2(4000)
);

UPDATE ${datasource.user}.pad_location_details
SET psr_submitted_option = 'YES'
WHERE psr_notification_submitted = 1;


UPDATE ${datasource.user}.pad_location_details
SET psr_submitted_option = 'NO'
WHERE psr_notification_submitted = 0;

ALTER TABLE ${datasource.user}.pad_location_details DROP COLUMN psr_notification_submitted;

ALTER TABLE ${datasource.user}.pad_location_details ADD (
    psr_not_required_reason VARCHAR2(4000),
    divers_used NUMBER(1)
    CONSTRAINT divers_used CHECK(divers_used IN (0, 1) OR divers_used IS NULL)
);
